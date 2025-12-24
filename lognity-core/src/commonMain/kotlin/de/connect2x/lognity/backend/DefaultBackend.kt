package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.config.ConfigSpec
import de.connect2x.lognity.api.config.config
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.context.context
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.Logger.Name
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.platformConsoleAppender
import de.connect2x.lognity.format.SimpleFormatter
import de.connect2x.lognity.logger.DefaultLogger
import de.connect2x.lognity.logger.DefaultMarker
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal expect fun getDefaultLogLevel(): Level

internal expect fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always
): Appender // @formatter:on

internal expect fun createSystemFileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    path: String
): Appender // @formatter:on

@OptIn(ExperimentalAtomicApi::class)
object DefaultBackend : Backend {
    override val name: String = "Lognity"
    override val defaultLevel: Level = getDefaultLogLevel()

    private val _configSpec: AtomicReference<ConfigSpec> = AtomicReference {
        platformConsoleAppender(
            "{{levelColor}}>>  {{levelSymbol}}\t{{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}"
        )
    }

    override var configSpec: ConfigSpec
        get() = _configSpec.load()
        set(value) {
            _configSpec.store(value)
        }

    private val _contextSpec: AtomicReference<ContextSpec> = AtomicReference {}

    override var contextSpec: ContextSpec
        get() = _contextSpec.load()
        set(value) {
            _contextSpec.store(value)
        }

    override val defaultFormatter: Formatter get() = SimpleFormatter.default

    override fun addShutdownHook(hook: () -> Unit) = ShutdownHandler.register(hook)

    override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker {
        return DefaultMarker(key, name, isEnabled)
    }

    override fun createLogger(name: String?, contextSpec: ContextSpec): Logger {
        return DefaultLogger(config(configSpec), context {
            this@DefaultBackend.contextSpec(this)
            contextSpec()
            name?.let(::Name)?.let(::value)
        })
    }
}