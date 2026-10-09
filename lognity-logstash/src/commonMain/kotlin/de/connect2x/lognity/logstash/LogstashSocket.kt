package de.connect2x.lognity.logstash

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Implementations must support cancellation of connect/write/flush, and nonblocking, idempotent close. */
fun interface LogstashSocketFactory {
    suspend fun connect(destination: LogstashDestination, config: LogstashConfig): LogstashSocket
}

interface LogstashSocket {
    val isClosed: Boolean
    suspend fun write(bytes: ByteArray)
    suspend fun flush()
    suspend fun shutdown() {
        flush(); close()
    }

    fun close()
}

object KtorLogstashSocketFactory : LogstashSocketFactory {
    override suspend fun connect(destination: LogstashDestination, config: LogstashConfig): LogstashSocket {
        val selector = SelectorManager(Dispatchers.IO)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        var socket: Socket? = null
        try {
            val tcp =
                aSocket(selector).tcp().connect(destination.host, destination.port, configure = config.socketOptions)
            socket = tcp
            val connection = config.tls?.let { configure ->
                tcp.tls(scope.coroutineContext) {
                    serverName = destination.host
                    configure()
                }
            } ?: tcp
            socket = connection
            val output = connection.openWriteChannel(autoFlush = false)
            val input = connection.openReadChannel()
            val result = object : LogstashSocket {
                override val isClosed: Boolean get() = connection.socketContext.isCompleted || input.isClosedForRead
                override suspend fun write(bytes: ByteArray) = output.writeFully(bytes)
                override suspend fun flush() = output.flush()
                override suspend fun shutdown() {
                    output.flushAndClose()
                    connection.close()
                    connection.socketContext.join()
                    tcp.socketContext.join()
                }

                override fun close() {
                    output.close(CancellationException("Logstash connection closed"))
                    input.cancel()
                    connection.close()
                    tcp.close()
                    scope.cancel()
                    tcp.socketContext.invokeOnCompletion { selector.close() }
                }
            }
            scope.launch {
                try {
                    while (!input.isClosedForRead) input.readByte()
                } catch (_: Exception) {
                    // The writer observes closure and performs reconnection.
                }
                finally {
                    connection.close()
                }
            }
            return result
        } catch (cause: Throwable) {
            socket?.close()
            scope.cancel()
            selector.close()
            throw cause
        }
    }
}

internal expect val supportsLogstashTls: Boolean