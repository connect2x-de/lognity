@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, de.connect2x.lognity.api.ExperimentalLoggingApi::class)

package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.logger.Level
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class LogstashAppenderTest {
    private val logger = TestLogger()
    private val plain = LogstashEncoder { (it.message + "\n").encodeToByteArray() }

    @Test
    fun appendsAsynchronouslyAndFlushesInOrder() = runTest {
        val socket = RecordingSocket()
        val appender = LogstashAppender(config(socket), plain)
        try {
            appender.append(logger, Level.INFO, "one", null)
            appender.appendSuspend(logger, Level.INFO, "two", null)
            assertTrue(socket.writes.isEmpty())
            appender.flushSuspend()
            assertEquals("one\ntwo\n", socket.writes.joinToString(""))
            assertEquals(0, appender.droppedEventCount)
        }
        finally {
            appender.stopSuspend()
        }
        assertTrue(socket.isClosed)
    }

    @Test
    fun fullQueueDropsImmediatelyByDefault() = runTest {
        val gate = CompletableDeferred<Unit>()
        val socket = RecordingSocket()
        val appender = LogstashAppender(
            config(socket).copy(
                ringBufferSize = 1, socketFactory = LogstashSocketFactory { _, _ -> gate.await(); socket }), plain
        )
        try {
            runCurrent()
            appender.append(logger, Level.INFO, "accepted", null)
            appender.append(logger, Level.INFO, "dropped", null)
            assertEquals(1, appender.droppedEventCount)
            gate.complete(Unit)
            appender.flushSuspend()
            assertEquals("accepted\n", socket.writes.joinToString(""))
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun positiveAppendTimeoutWaitsThenDropsExactlyOnce() = runTest {
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
            ringBufferSize = 1,
            appendTimeout = 50.milliseconds,
            socketFactory = LogstashSocketFactory { _, _ -> awaitCancellation() }), plain
        )
        try {
            runCurrent()
            appender.append(logger, Level.INFO, "one", null)
            val append = async { appender.appendSuspend(logger, Level.INFO, "two", null) }
            runCurrent()
            assertFalse(append.isCompleted)
            advanceTimeBy(50)
            runCurrent()
            append.await()
            assertEquals(1, appender.droppedEventCount)
        }
        finally {
            appender.stopSuspend()
        }
        assertEquals(2, appender.droppedEventCount)
    }

    @Test
    fun negativeAppendTimeoutWaitsUntilSpaceIsAvailable() = runTest {
        val gate = CompletableDeferred<Unit>()
        val socket = RecordingSocket()
        val appender = LogstashAppender(
            config(socket).copy(
            ringBufferSize = 1,
            appendTimeout = (-1).milliseconds,
            socketFactory = LogstashSocketFactory { _, _ -> gate.await(); socket }), plain
        )
        try {
            runCurrent()
            appender.append(logger, Level.INFO, "one", null)
            val append = async { appender.appendSuspend(logger, Level.INFO, "two", null) }
            runCurrent()
            assertFalse(append.isCompleted)
            gate.complete(Unit)
            append.await()
            appender.flushSuspend()
            assertEquals("one\ntwo\n", socket.writes.joinToString(""))
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun shutdownAccountsForAnEventRemovedBeforeConnecting() = runTest {
        val dropped = mutableListOf<String>()
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
                socketFactory = LogstashSocketFactory { _, _ -> awaitCancellation() },
                listeners = listOf(object : LogstashListener {
                    override fun eventDropped(event: LogstashEvent, reason: LogstashDropReason) {
                        dropped += event.message
                    }
                })
            ), plain
        )
        appender.append(logger, Level.INFO, "in-flight", null)
        appender.append(logger, Level.INFO, "queued", null)
        runCurrent()
        appender.stopSuspend()
        assertEquals(setOf("in-flight", "queued"), dropped.toSet())
        assertEquals(2, appender.droppedEventCount)
        assertFalse(appender.isStarted)
    }

    @Test
    fun zeroGraceShutdownAccountsForEventsBeforeWriterStarts() = runTest {
        val appender = LogstashAppender(config(RecordingSocket()).copy(shutdownGracePeriod = 0.seconds), plain)
        appender.append(logger, Level.INFO, "queued", null)
        appender.stopSuspend()
        assertEquals(1, appender.droppedEventCount)
        assertFalse(appender.isStarted)
    }

    @Test
    fun failoverReconnectionAndPerDestinationDelay() = runTest {
        val attempts = mutableListOf<Pair<String, Long>>()
        val socket = RecordingSocket()
        val appender = LogstashAppender(
            config(socket).copy(
            destinations = listOf(LogstashDestination("primary"), LogstashDestination("secondary")),
            socketFactory = LogstashSocketFactory { destination, _ ->
                attempts += destination.host to testScheduler.currentTime
                if (attempts.size <= 2) error("unavailable")
                socket
            }), plain
        )
        try {
            appender.append(logger, Level.INFO, "retry", null)
            appender.flushSuspend()
            assertEquals(listOf("primary" to 0L, "secondary" to 0L, "primary" to 100L), attempts)
            assertEquals("retry\n", socket.writes.single())
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun failedWritesReconnectAndRetryTheBatch() = runTest {
        val broken = RecordingSocket { error("broken pipe") }
        val recovered = RecordingSocket()
        var attempts = 0
        val appender = LogstashAppender(config(recovered).copy(socketFactory = LogstashSocketFactory { _, _ ->
            if (attempts++ == 0) broken else recovered
        }), plain)
        try {
            appender.append(logger, Level.INFO, "retry", null)
            appender.flushSuspend()
            assertTrue(broken.isClosed)
            assertEquals("retry\n", recovered.writes.single())
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun writeTimeoutReconnectsInsteadOfStallingTheQueue() = runTest {
        val blocked = RecordingSocket { awaitCancellation() }
        val recovered = RecordingSocket()
        var attempts = 0
        val appender = LogstashAppender(
            config(recovered).copy(
            writeTimeout = 50.milliseconds,
            socketFactory = LogstashSocketFactory { _, _ -> if (attempts++ == 0) blocked else recovered }), plain
        )
        try {
            appender.append(logger, Level.INFO, "retry", null)
            appender.flushSuspend()
            assertTrue(blocked.isClosed)
            assertEquals("retry\n", recovered.writes.single())
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun connectionTimeoutAttemptsTheNextDestination() = runTest {
        val socket = RecordingSocket()
        val attempts = mutableListOf<String>()
        val appender = LogstashAppender(
            config(socket).copy(
            destinations = listOf(LogstashDestination("primary"), LogstashDestination("secondary")),
            connectionTimeout = 25.milliseconds,
            socketFactory = LogstashSocketFactory { destination, _ ->
                attempts += destination.host
                if (destination.host == "primary") awaitCancellation()
                socket
            }), plain
        )
        try {
            appender.append(logger, Level.INFO, "one", null)
            appender.flushSuspend()
            assertEquals(listOf("primary", "secondary"), attempts)
            assertEquals(25L, testScheduler.currentTime)
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun idleKeepAliveUsesConfiguredBytesAndResetsAfterEvents() = runTest {
        val socket = RecordingSocket()
        val appender = LogstashAppender(
            config(socket).copy(keepAliveDuration = 100.milliseconds, keepAliveMessage = "PING\n"), plain
        )
        try {
            runCurrent()
            advanceTimeBy(100)
            runCurrent()
            assertEquals(listOf("PING\n"), socket.writes)
            advanceTimeBy(50)
            appender.append(logger, Level.INFO, "event", null)
            appender.flushSuspend()
            advanceTimeBy(99)
            runCurrent()
            assertEquals(2, socket.writes.size)
            advanceTimeBy(1)
            runCurrent()
            assertEquals("PING\n", socket.writes.last())
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun roundRobinTtlRotatesConnectionsWhileIdle() = runTest {
        val sockets = mutableListOf<RecordingSocket>()
        val destinations = mutableListOf<String>()
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
            destinations = listOf(LogstashDestination("one"), LogstashDestination("two")),
            connectionStrategy = RoundRobinStrategy(100.milliseconds),
            socketFactory = LogstashSocketFactory { destination, _ ->
                destinations += destination.host
                RecordingSocket().also { sockets += it }
            }), plain
        )
        try {
            runCurrent()
            advanceTimeBy(100)
            runCurrent()
            assertEquals(listOf("one", "two"), destinations)
            assertTrue(sockets.first().isClosed)
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun secondaryTtlReturnsToPrimaryEvenBeforeMinimumConnectionTime() = runTest {
        val attempts = mutableListOf<String>()
        val sockets = mutableListOf<RecordingSocket>()
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
            destinations = listOf(LogstashDestination("primary"), LogstashDestination("secondary")),
            connectionStrategy = PreferPrimaryStrategy(secondaryConnectionTTL = 100.milliseconds),
            socketFactory = LogstashSocketFactory { destination, _ ->
                attempts += destination.host
                if (attempts.size == 1) error("primary unavailable")
                RecordingSocket().also { sockets += it }
            }), plain
        )
        try {
            runCurrent()
            advanceTimeBy(100)
            runCurrent()
            assertEquals(listOf("primary", "secondary", "primary"), attempts)
            assertTrue(sockets.first().isClosed)
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun emptyHeartbeatDisablesKeepAliveAndZeroBufferWritesEachEvent() = runTest {
        val socket = RecordingSocket()
        val appender = LogstashAppender(
            config(socket).copy(
                keepAliveDuration = 10.milliseconds, keepAliveMessage = "", writeBufferSize = 0
            ), plain
        )
        try {
            appender.append(logger, Level.INFO, "one", null)
            appender.append(logger, Level.INFO, "two", null)
            appender.flushSuspend()
            advanceTimeBy(100)
            runCurrent()
            assertEquals(listOf("one\n", "two\n"), socket.writes)
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun invalidCustomStrategyStopsWriterAndReleasesPendingFlush() = runTest {
        val failed = CompletableDeferred<Throwable>()
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
                connectionStrategy = object : DestinationConnectionStrategy {
                override fun selectDestination(
                    destinationCount: Int, previousIndex: Int, connectionDuration: kotlin.time.Duration?
                ) = -2
            }, listeners = listOf(object : LogstashListener {
                override fun writerFailed(cause: Throwable) {
                    failed.complete(cause)
                }
            })
            ), plain
        )
        appender.append(logger, Level.INFO, "bad strategy", null)
        assertFailsWith<IllegalStateException> { appender.flushSuspend() }
        assertTrue(failed.await() is IllegalArgumentException)
        appender.stopSuspend()
        assertEquals(1, appender.droppedEventCount)
    }

    @Test
    fun encodingAndListenerFailuresDoNotStopSubsequentEvents() = runTest {
        val failed = mutableListOf<String>()
        val socket = RecordingSocket()
        val appender = LogstashAppender(config(socket).copy(listeners = listOf(object : LogstashListener {
            override fun connectionOpened(destination: LogstashDestination) {
                error("listener")
            }

            override fun eventFailed(event: LogstashEvent, cause: Throwable) {
                failed += event.message
            }
        })), LogstashEncoder { if (it.message == "bad") error("encoder") else plain.encode(it) })
        try {
            appender.append(logger, Level.INFO, "bad", null)
            appender.append(logger, Level.INFO, "good", null)
            appender.flushSuspend()
            assertEquals(listOf("bad"), failed)
            assertEquals("good\n", socket.writes.single())
            assertTrue(appender.isStarted)
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun filterRunsBeforeEncodingAndShutdownDrainsWithHeaderAndFooter() = runTest {
        val socket = RecordingSocket()
        val appender = LogstashAppender(config(socket), object : LogstashEncoder {
            override fun encode(event: LogstashEvent) = plain.encode(event)
            override fun header() = "HEADER\n".encodeToByteArray()
            override fun footer() = "FOOTER\n".encodeToByteArray()
        }, filter = Filter { _, level, _, _ -> level >= Level.INFO })
        appender.append(logger, Level.DEBUG, "filtered", null)
        appender.append(logger, Level.INFO, "accepted", null)
        appender.stopSuspend()
        assertEquals("HEADER\naccepted\nFOOTER\n", socket.writes.joinToString(""))
        assertTrue(socket.isClosed)
        appender.stopSuspend()
        appender.append(logger, Level.INFO, "after stop", null)
        assertEquals(1, appender.droppedEventCount)
        assertFailsWith<IllegalStateException> { appender.start() }
    }

    @Test
    fun flushTimesOutWhenDestinationIsUnavailable() = runTest {
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
            flushTimeout = 50.milliseconds, socketFactory = LogstashSocketFactory { _, _ -> awaitCancellation() }),
            plain
        )
        try {
            appender.append(logger, Level.INFO, "offline", null)
            assertFailsWith<TimeoutCancellationException> { appender.flushSuspend() }
        }
        finally {
            appender.stopSuspend()
        }
    }

    @Test
    fun cancelledProducerDoesNotLeaveAnEventInTheQueue() = runTest {
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
            ringBufferSize = 1,
            appendTimeout = (-1).seconds,
            socketFactory = LogstashSocketFactory { _, _ -> awaitCancellation() }), plain
        )
        runCurrent()
        appender.append(logger, Level.INFO, "one", null)
        val producer = launch { appender.appendSuspend(logger, Level.INFO, "cancelled", null) }
        runCurrent()
        producer.cancel()
        producer.join()
        appender.stopSuspend()
        assertEquals(2, appender.droppedEventCount)
    }

    @Test
    fun shutdownReleasesBlockedProducerWithoutCancellingItsCaller() = runTest {
        val appender = LogstashAppender(
            config(RecordingSocket()).copy(
            ringBufferSize = 1,
            appendTimeout = (-1).seconds,
            socketFactory = LogstashSocketFactory { _, _ -> awaitCancellation() }), plain
        )
        runCurrent()
        appender.append(logger, Level.INFO, "queued", null)
        val producer = async { appender.appendSuspend(logger, Level.INFO, "waiting", null) }
        runCurrent()
        appender.stopSuspend()
        producer.await()
        assertEquals(2, appender.droppedEventCount)
    }

    @Test
    fun manualStartAndStopWithoutStartingAreSafe() = runTest {
        var connections = 0
        val appender = LogstashAppender(config(RecordingSocket()).copy(socketFactory = LogstashSocketFactory { _, _ ->
            connections++
            RecordingSocket()
        }), autoStart = false)
        assertFalse(appender.isStarted)
        appender.append(logger, Level.INFO, "not started", null)
        appender.stopSuspend()
        assertEquals(0, connections)
        assertEquals(1, appender.droppedEventCount)
    }

    private fun TestScope.config(socket: RecordingSocket) = LogstashConfig(
        destinations = listOf(LogstashDestination("localhost")),
        socketFactory = LogstashSocketFactory { _, _ -> socket },
        reconnectionDelay = 100.milliseconds,
        shutdownGracePeriod = 100.milliseconds,
        dispatcher = StandardTestDispatcher(testScheduler),
        timeSource = testScheduler.timeSource
    )
}