package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
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

    private val appenders: ArrayList<Appender> = ArrayList()
    private val overrides: ArrayList<Override> = ArrayList()

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
        overrides += config.overrides
        return this
    }

    /**
     * Adds a new appender instance to this logger config if not already present.
     *
     * @param appender The appender instance to add to this config.
     */
    fun appender(appender: Appender) {
        if (appender in appenders) return
        val name = appender.name
        if (name != null) require(appenders.none { appender -> appender.name == name }) {
            "Appender with name '$name' is already present"
        }
        appenders += appender
    }

    /**
     * Adds a new override instance to this logger config if not already present.
     *
     * @param override The override instance to add to this config.
     */
    fun override(override: Override) {
        if (override in overrides) return
        overrides += override
    }

    /**
     * Adds a new override instance built from the given spec to
     * this logger config if not already present.
     *
     * @param spec The override spec to build an override from to add to this config.
     */
    inline fun override(spec: OverrideSpec) {
        contract {
            callsInPlace(spec, InvocationKind.EXACTLY_ONCE)
        }
        override(OverrideBuilder().apply(spec).build())
    }

    @PublishedApi
    internal fun build(): Config = Config(level, isEnabled, appenders, overrides)
}

/**
 * Type alias for a Config builder specification used by [Config].
 */
typealias ConfigSpec = ConfigBuilder.() -> Unit

/**
 * Creates a new immutable [Config] using the provided [spec] DSL.
 *
 * @param spec The DSL specification to build the configuration.
 * @return A new [Config] instance.
 */
inline fun Config(spec: ConfigSpec): Config {
    contract {
        callsInPlace(spec, InvocationKind.EXACTLY_ONCE)
    }
    return ConfigBuilder().apply(spec).build()
}