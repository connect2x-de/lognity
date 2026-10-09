package de.connect2x.lognity.logstash

import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** A TCP destination. IPv6 literals in [parse] must be enclosed in brackets. */
data class LogstashDestination(val host: String, val port: Int = 4560) {
    init {
        require(host.isNotBlank() && host.none { it.isWhitespace() || it in "[],/" }) { "Invalid destination host" }
        require(port in 0..65535) { "Destination port must be in 0..65535" }
    }

    companion object {
        fun parse(value: String): LogstashDestination {
            val destination = value.trim()
            val host: String
            val suffix: String
            if (destination.startsWith('[')) {
                val end = destination.indexOf(']')
                require(end > 1) { "Invalid bracketed destination: $value" }
                host = destination.substring(1, end)
                suffix = destination.substring(end + 1)
            }
            else {
                require(destination.count { it == ':' } <= 1) { "IPv6 destinations must be bracketed" }
                host = destination.substringBefore(':')
                suffix = destination.removePrefix(host)
            }
            require(suffix.isEmpty() || suffix.startsWith(':')) { "Invalid destination: $value" }
            val port = if (suffix.isEmpty()) 4560
            else {
                val text = suffix.drop(1)
                require(text.isNotEmpty() && text.all { it in '0'..'9' }) { "Invalid destination port: $value" }
                requireNotNull(text.toIntOrNull()) { "Invalid destination port: $value" }
            }
            return LogstashDestination(host, port)
        }
    }
}

/**
 * Immutable programmatic TCP appender configuration, with LogstashTcpSocketAppender defaults.
 *
 * [appendTimeout] is zero to drop on a full queue, negative to wait indefinitely, or positive to wait
 * for that duration. [connectionTimeout] and [writeTimeout] are zero to disable their timeout.
 * [keepAliveDuration] is null to disable heartbeats; [keepAliveMessage] is sent verbatim as UTF-8.
 * Use [keepAliveBytes] for another charset. [writeBufferSize] zero disables batching.
 *
 * [tls] enables TLS and configures Ktor's TLS builder. It is supported on JVM/Android only.
 * Native TLS requests fail at construction rather than silently falling back to plaintext.
 * [socketOptions] configures low-level TCP options. [socketFactory] permits custom transports.
 * Callbacks and strategies must be thread-safe, fast, and must not flush or stop this appender.
 */
data class LogstashConfig(
    val destinations: List<LogstashDestination>,
    val connectionStrategy: DestinationConnectionStrategy = PreferPrimaryStrategy(),
    val ringBufferSize: Int = 8192,
    val appendTimeout: Duration = Duration.ZERO,
    val reconnectionDelay: Duration = 30.seconds,
    val connectionTimeout: Duration = 5.seconds,
    val writeTimeout: Duration = Duration.ZERO,
    val writeBufferSize: Int = 8192,
    val keepAliveDuration: Duration? = null,
    val keepAliveMessage: String = "\n",
    val keepAliveBytes: ByteArray? = null,
    val shutdownGracePeriod: Duration = 60.seconds,
    val flushTimeout: Duration = 60.seconds,
    val tls: (TLSConfigBuilder.() -> Unit)? = null,
    val socketOptions: SocketOptions.TCPClientSocketOptions.() -> Unit = {},
    val socketFactory: LogstashSocketFactory = KtorLogstashSocketFactory,
    val listeners: List<LogstashListener> = emptyList(),
    val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
    val timeSource: TimeSource = TimeSource.Monotonic
) {
    init {
        require(destinations.isNotEmpty()) { "At least one destination is required" }
        require(ringBufferSize > 0 && ringBufferSize and (ringBufferSize - 1) == 0) {
            "ringBufferSize must be a positive power of two"
        }
        require(writeBufferSize >= 0) { "writeBufferSize must not be negative" }
        require(reconnectionDelay.isFinite() && reconnectionDelay > Duration.ZERO) { "reconnectionDelay must be positive and finite" }
        require(connectionTimeout.isFinite() && connectionTimeout >= Duration.ZERO) { "Invalid connectionTimeout" }
        require(writeTimeout.isFinite() && writeTimeout >= Duration.ZERO) { "Invalid writeTimeout" }
        require(keepAliveDuration == null || keepAliveDuration.isFinite() && keepAliveDuration > Duration.ZERO) { "Invalid keepAliveDuration" }
        require(shutdownGracePeriod.isFinite() && shutdownGracePeriod >= Duration.ZERO) { "Invalid shutdownGracePeriod" }
        require(flushTimeout.isFinite() && flushTimeout > Duration.ZERO) { "Invalid flushTimeout" }
        require(tls == null || socketFactory !== KtorLogstashSocketFactory || supportsLogstashTls) {
            "Ktor TLS is supported on JVM and Android, not on Native"
        }
    }
}