package de.connect2x.lognity.config.appender

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.config.SerializableFilter
import de.connect2x.lognity.config.logstash.LogstashConnectionStrategy
import de.connect2x.lognity.config.logstash.LogstashJsonEncoder
import de.connect2x.lognity.config.serialization.RefOrValue
import de.connect2x.lognity.logstash.LogstashConfig
import de.connect2x.lognity.logstash.LogstashDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Serializable TCP appender options. Durations are ISO-8601 strings; destinations use host[:port]. */
@ExperimentalLoggingApi
@SerialName("logstash")
@Serializable
data class LogstashAppender(
    val destinations: RefOrValue<List<RefOrValue<String>>>,
    override val pattern: RefOrValue<String> = RefOrValue.Value("{{message}}"),
    override val formatter: RefOrValue<String> = RefOrValue.Value("default"),
    override val filter: RefOrValue<SerializableFilter> = RefOrValue.Value(SerializableFilter()),
    override val name: RefOrValue<String?> = RefOrValue.Value(null),
    @SerialName("connection_strategy") val connectionStrategy: RefOrValue<LogstashConnectionStrategy> = RefOrValue.Value(
        LogstashConnectionStrategy.PreferPrimary()
    ),
    @SerialName("ring_buffer_size") val ringBufferSize: RefOrValue<Int> = RefOrValue.Value(8192),
    @SerialName("append_timeout") val appendTimeout: RefOrValue<Duration> = RefOrValue.Value(Duration.ZERO),
    @SerialName("reconnection_delay") val reconnectionDelay: RefOrValue<Duration> = RefOrValue.Value(30.seconds),
    @SerialName("connection_timeout") val connectionTimeout: RefOrValue<Duration> = RefOrValue.Value(5.seconds),
    @SerialName("write_timeout") val writeTimeout: RefOrValue<Duration> = RefOrValue.Value(Duration.ZERO),
    @SerialName("write_buffer_size") val writeBufferSize: RefOrValue<Int> = RefOrValue.Value(8192),
    @SerialName("keep_alive_duration") val keepAliveDuration: RefOrValue<Duration?> = RefOrValue.Value(null),
    @SerialName("keep_alive_message") val keepAliveMessage: RefOrValue<String> = RefOrValue.Value("\n"),
    @SerialName("keep_alive_bytes") val keepAliveBytes: RefOrValue<ByteArray?> = RefOrValue.Value(null),
    @SerialName("shutdown_grace_period") val shutdownGracePeriod: RefOrValue<Duration> = RefOrValue.Value(60.seconds),
    @SerialName("flush_timeout") val flushTimeout: RefOrValue<Duration> = RefOrValue.Value(60.seconds),
    val tls: RefOrValue<Boolean> = RefOrValue.Value(false),
    val encoder: RefOrValue<LogstashJsonEncoder> = RefOrValue.Value(LogstashJsonEncoder())
) : AbstractSerializableAppender() {
    internal fun asLogstashConfig(): LogstashConfig = LogstashConfig(
        destinations = destinations.resolveTemplate(config)
            .map { LogstashDestination.parse(it.resolveTemplate(config)) },
        connectionStrategy = connectionStrategy.resolveTemplate(config).asStrategy(config),
        ringBufferSize = ringBufferSize.resolveTemplate(config),
        appendTimeout = appendTimeout.resolveTemplate(config),
        reconnectionDelay = reconnectionDelay.resolveTemplate(config),
        connectionTimeout = connectionTimeout.resolveTemplate(config),
        writeTimeout = writeTimeout.resolveTemplate(config),
        writeBufferSize = writeBufferSize.resolveTemplate(config),
        keepAliveDuration = keepAliveDuration.resolveTemplate(config),
        keepAliveMessage = keepAliveMessage.resolveTemplate(config),
        keepAliveBytes = keepAliveBytes.resolveTemplate(config),
        shutdownGracePeriod = shutdownGracePeriod.resolveTemplate(config),
        flushTimeout = flushTimeout.resolveTemplate(config),
        tls = if (tls.resolveTemplate(config)) ({}) else null
    )
}