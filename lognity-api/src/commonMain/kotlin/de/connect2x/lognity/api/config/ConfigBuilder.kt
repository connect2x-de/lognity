package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A builder class for creating a new [Config] instance
 * using a simple DSL.
 */
@ConfigDsl
class ConfigBuilder @PublishedApi internal constructor() {
    /**
     * The initial log level used by the logger instance(s)
     * using this config.
     */
    var level: Level = Level.default

    /**
     * The initial enable state applied to the logger instance(s)
     * using this config.
     */
    var isEnabled: Boolean = true

    @PublishedApi
    internal val appenders: ArrayList<Appender> = ArrayList()

    /**
     * Copies all config properties from the given [Config] instance
     * into this builder instance.
     *
     * @param config The config from which to copy all properties into this builder instance.
     * @return This builder instance.
     */
    fun setFrom(config: Config): ConfigBuilder {
        level = config.initialLevel
        isEnabled = config.initialEnableState
        appenders += config.appenders
        return this
    }

    /**
     * Adds a new appender instance to this logger config if not already present.
     *
     * @param appender The appender instance to add to this config.
     */
    fun appender(appender: Appender) {
        require(appender !in appenders) { "Appender is already present" }
        val name = appender.name
        if (name != null) require(appenders.none { appender -> appender.name == name }) {
            "Appender with name '$name' is already present"
        }
        appenders += appender
    }

    @PublishedApi
    internal fun build(): Config = Config(level, isEnabled, appenders)
}

/**
 * Type alias for a Config builder specification used by [Config].
 */
typealias ConfigSpec = ConfigBuilder.() -> Unit

/**
 * Creates a new immutable [Config] using the provided [spec] DSL.
 */
@OptIn(ExperimentalContracts::class)
inline fun Config(spec: ConfigSpec): Config {
    contract {
        callsInPlace(spec, InvocationKind.EXACTLY_ONCE)
    }
    return ConfigBuilder().apply(spec).build()
}