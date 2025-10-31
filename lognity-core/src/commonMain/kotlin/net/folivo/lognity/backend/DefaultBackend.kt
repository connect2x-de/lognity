package net.folivo.lognity.backend

import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.ConfigSpec
import net.folivo.lognity.api.config.config
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.context.ContextSpec
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.context.context
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.config.platformConsoleAppender
import net.folivo.lognity.format.SimpleFormatter
import net.folivo.lognity.logger.DefaultLogger
import net.folivo.lognity.logger.DefaultMarker
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
            if (name != null) this[Logger.Name] = Logger.Name(name)
        })
    }
}