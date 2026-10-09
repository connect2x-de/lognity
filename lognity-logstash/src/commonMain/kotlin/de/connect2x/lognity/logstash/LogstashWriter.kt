package de.connect2x.lognity.logstash

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark

internal class LogstashWriter(
    private val appender: LogstashAppender, private val queue: Channel<LogstashCommand>
) {
    private val config = appender.config
    private val encoder = appender.encoder
    private var socket: LogstashSocket? = null
    private var destinationIndex = -1
    private var connectedAt: TimeMark? = null
    private var connectionDuration: Duration? = null
    private val attemptedAt = arrayOfNulls<TimeMark>(config.destinations.size)
    private var lastWrite = config.timeSource.markNow()
    private val heartbeat = config.keepAliveBytes ?: config.keepAliveMessage.encodeToByteArray()
    private val batch = ArrayList<Pair<LogstashCommand.Event, ByteArray>>()
    private var batchSize = 0
    private var current: LogstashCommand? = null

    suspend fun run() {
        try {
            while (currentCoroutineContext().isActive) {
                val next = queue.tryReceive()
                if (next.isClosed) break
                current = next.getOrNull()
                maintainConnection()
                if (current == null) {
                    flushBatch()
                    current = withTimeoutOrNull(nextMaintenance()) { queue.receiveCatching().getOrNull() }
                    if (current == null) continue
                }
                when (val command = current) {
                    is LogstashCommand.Event -> encode(command)
                    is LogstashCommand.Flush -> {
                        flushBatch()
                        command.completion.complete(Unit)
                    }

                    null -> Unit
                }
                current = null
            }
            flushBatch()
            closeGracefully()
        }
        finally {
            disconnect()
            batch.forEach { (command, _) -> appender.drop(command, LogstashDropReason.SHUTDOWN) }
            when (val command = current) {
                is LogstashCommand.Event -> appender.drop(command, LogstashDropReason.SHUTDOWN)
                is LogstashCommand.Flush -> command.completion.completeExceptionally(IllegalStateException("Writer stopped before flushing"))
                null -> Unit
            }
        }
    }

    private suspend fun encode(command: LogstashCommand.Event) {
        val bytes = try {
            encoder.encode(command.event)
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            command.completed.store(true)
            appender.notify { eventFailed(command.event, cause) }
            return
        }
        if (batch.isNotEmpty() && bytes.size > config.writeBufferSize - batchSize) flushBatch()
        batch.add(command to bytes)
        batchSize += bytes.size
        if (config.writeBufferSize == 0 || batchSize >= config.writeBufferSize) flushBatch()
    }

    private suspend fun flushBatch() {
        if (batch.isEmpty()) return
        val bytes = ByteArray(batchSize)
        var offset = 0
        batch.forEach { (_, encoded) ->
            encoded.copyInto(bytes, offset)
            offset += encoded.size
        }
        send(bytes)
        batch.forEach { (command, encoded) ->
            command.completed.store(true)
            appender.notify { eventSent(command.event, encoded.size) }
        }
        batch.clear()
        batchSize = 0
    }

    private suspend fun maintainConnection() {
        val connection = socket
        if (connection != null) {
            if (connection.isClosed) disconnect()
            else config.connectionStrategy.connectionTTL(destinationIndex)?.let { ttl ->
                if (connectedAt!!.elapsedNow() >= ttl) {
                    flushBatch()
                    closeGracefully()
                    if (config.connectionStrategy is PreferPrimaryStrategy) destinationIndex = -1
                }
            }
        }
        ensureConnected()
        val keepAlive = config.keepAliveDuration
        if (keepAlive != null && heartbeat.isNotEmpty() && lastWrite.elapsedNow() >= keepAlive) send(heartbeat)
    }

    private fun nextMaintenance(): Duration {
        var duration = 1.seconds
        config.keepAliveDuration?.let {
            if (heartbeat.isNotEmpty()) duration = minOf(duration, it - lastWrite.elapsedNow())
        }
        config.connectionStrategy.connectionTTL(destinationIndex)
            ?.let { duration = minOf(duration, it - connectedAt!!.elapsedNow()) }
        return duration.coerceAtLeast(Duration.ZERO)
    }

    private suspend fun ensureConnected() {
        while (socket == null) {
            currentCoroutineContext().ensureActive()
            destinationIndex = config.connectionStrategy.selectDestination(
                config.destinations.size,
                destinationIndex,
                connectionDuration
            )
            require(destinationIndex in config.destinations.indices) { "Connection strategy returned an invalid destination index" }
            connectionDuration = null
            attemptedAt[destinationIndex]?.let { delay(config.reconnectionDelay - it.elapsedNow()) }
            attemptedAt[destinationIndex] = config.timeSource.markNow()
            val destination = config.destinations[destinationIndex]
            var connection: LogstashSocket? = null
            try {
                timed(config.connectionTimeout) {
                    connection = config.socketFactory.connect(destination, config)
                }
                socket = connection
                connectedAt = config.timeSource.markNow()
                val header = encoder.header()
                if (header.isNotEmpty()) write(header)
                lastWrite = config.timeSource.markNow()
                appender.notify { connectionOpened(destination) }
            } catch (cause: Throwable) {
                connection?.close()
                socket = null
                connectedAt = null
                currentCoroutineContext().ensureActive()
                appender.notify { connectionFailed(destination, cause) }
            }
        }
    }

    private suspend fun send(bytes: ByteArray) {
        while (true) {
            ensureConnected()
            try {
                write(bytes)
                lastWrite = config.timeSource.markNow()
                return
            } catch (cause: Throwable) {
                disconnect()
                currentCoroutineContext().ensureActive()
                appender.notify { connectionFailed(config.destinations[destinationIndex], cause) }
            }
        }
    }

    private suspend fun write(bytes: ByteArray) {
        timed(config.writeTimeout) {
            val connection = checkNotNull(socket)
            check(!connection.isClosed) { "Socket closed by peer" }
            connection.write(bytes)
            connection.flush()
        }
    }

    private suspend fun closeGracefully() {
        if (socket == null) return
        try {
            val footer = encoder.footer()
            if (footer.isNotEmpty()) write(footer)
            timed(config.writeTimeout) { socket?.shutdown() }
        } catch (cause: Throwable) {
            currentCoroutineContext().ensureActive()
            appender.notify { connectionFailed(config.destinations[destinationIndex], cause) }
        }
        finally {
            disconnect()
        }
    }

    private fun disconnect() {
        val connection = socket ?: return
        socket = null
        connectionDuration = connectedAt?.elapsedNow()
        connectedAt = null
        try {
            connection.close()
        }
        finally {
            appender.notify { connectionClosed(config.destinations[destinationIndex]) }
        }
    }

    private suspend fun <T> timed(timeout: Duration, block: suspend () -> T): T =
        if (timeout == Duration.ZERO) block() else withTimeout(timeout) { block() }
}