package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.backend.Platform
import de.connect2x.lognity.api.logger.Level

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
        appenders += appender
    }

    /**
     * Applies the given configuration only for the given platform.
     */
    inline fun onlyOn(platform: Platform, spec: ConfigSpec) {
        if (platform != Backend.platform) return
        spec()
    }

    /**
     * Applies the given configuration only for the given platform.
     */
    inline fun onlyOn(platforms: Set<Platform>, spec: ConfigSpec) {
        if (Backend.platform !in platforms) return
        spec()
    }

    @PublishedApi
    internal fun build(): Config = Config(level, isEnabled, appenders)
}

/**
 * Type alias for a Config builder specification used by [config].
 */
typealias ConfigSpec = ConfigBuilder.() -> Unit

/**
 * Creates a new immutable [Config] using the provided [spec] DSL.
 */
inline fun config(spec: ConfigSpec): Config = ConfigBuilder().apply(spec).build()