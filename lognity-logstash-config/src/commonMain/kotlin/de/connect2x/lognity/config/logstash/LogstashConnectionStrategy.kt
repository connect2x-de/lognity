package de.connect2x.lognity.config.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.serialization.RefOrValue
import de.connect2x.lognity.logstash.DestinationConnectionStrategy
import de.connect2x.lognity.logstash.PreferPrimaryStrategy
import de.connect2x.lognity.logstash.RandomDestinationStrategy
import de.connect2x.lognity.logstash.RoundRobinStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Built-in destination selection strategies for the Logstash TCP appender. */
@ExperimentalLoggingApi
@Serializable
sealed class LogstashConnectionStrategy {
    internal abstract fun asStrategy(config: SerializableConfig): DestinationConnectionStrategy

    @SerialName("prefer_primary")
    @Serializable
    data class PreferPrimary(
        @SerialName("min_connection_time_before_primary") val minConnectionTimeBeforePrimary: RefOrValue<Duration> = RefOrValue.Value(
            10.seconds
        ), @SerialName("secondary_connection_ttl") val secondaryConnectionTTL: RefOrValue<Duration?> = RefOrValue.Value(
            null
        )
    ) : LogstashConnectionStrategy() {
        override fun asStrategy(config: SerializableConfig): DestinationConnectionStrategy = PreferPrimaryStrategy(
            minConnectionTimeBeforePrimary.resolveTemplate(config), secondaryConnectionTTL.resolveTemplate(config)
        )
    }

    @SerialName("round_robin")
    @Serializable
    data class RoundRobin(
        @SerialName("connection_ttl") val connectionTTL: RefOrValue<Duration?> = RefOrValue.Value(null)
    ) : LogstashConnectionStrategy() {
        override fun asStrategy(config: SerializableConfig): DestinationConnectionStrategy =
            RoundRobinStrategy(connectionTTL.resolveTemplate(config))
    }

    @SerialName("random")
    @Serializable
    data class Random(
        @SerialName("connection_ttl") val connectionTTL: RefOrValue<Duration?> = RefOrValue.Value(null)
    ) : LogstashConnectionStrategy() {
        override fun asStrategy(config: SerializableConfig): DestinationConnectionStrategy =
            RandomDestinationStrategy(connectionTTL.resolveTemplate(config))
    }
}