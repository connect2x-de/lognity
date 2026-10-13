@file:OptIn(ExperimentalLoggingApi::class)

package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.logger.Level
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalLoggingApi::class)
class LogstashConcurrencyTest {
    @Test
    fun concurrentProducersPreserveEachProducersOrderWithoutLosingEvents() = runBlocking {
        val socket = RecordingSocket()
        val appender = LogstashAppender(
            LogstashConfig(
            destinations = listOf(LogstashDestination("localhost")),
            ringBufferSize = 16,
            appendTimeout = (-1).seconds,
            socketFactory = LogstashSocketFactory { _, _ -> socket }),
            LogstashEncoder { (it.message + "\n").encodeToByteArray() })
        try {
            withTimeout(15.seconds) {
                coroutineScope {
                    repeat(8) { producer ->
                        launch(Dispatchers.Default) {
                            val logger = TestLogger()
                            repeat(100) { index ->
                                if (producer % 2 == 0) appender.append(logger, Level.INFO, "$producer:$index", null)
                                else appender.appendSuspend(logger, Level.INFO, "$producer:$index", null)
                            }
                        }
                    }
                }
                appender.flushSuspend()
                appender.stopSuspend()
                val messages = socket.writes.joinToString("").lineSequence().filter { it.isNotEmpty() }.toList()
                assertEquals(800, messages.size)
                assertEquals(800, messages.toSet().size)
                repeat(8) { producer ->
                    assertEquals((0 until 100).map { "$producer:$it" }, messages.filter { it.startsWith("$producer:") })
                }
                assertEquals(0, appender.droppedEventCount)
            }
        }
        finally {
            appender.stopSuspend()
        }
    }
}