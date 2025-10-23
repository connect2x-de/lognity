package net.folivo.lognity.backend

import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.config.ConfigSpec
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.ContextBuilder
import net.folivo.lognity.api.logger.ContextSpec
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.config.fileAppender
import net.folivo.lognity.config.platformConsoleAppender
import net.folivo.lognity.format.SimpleFormatter
import net.folivo.lognity.logger.DefaultLogger
import net.folivo.lognity.logger.DefaultMarker
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.ExperimentalTime

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
    override val name: String = "Skroll"
    override val defaultLevel: Level = getDefaultLogLevel()

    private val _configSpec: AtomicReference<ConfigSpec> = AtomicReference {
        platformConsoleAppender(
            "{{levelColor}}>>  {{levelSymbol}}\t{{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{thread}}) {{message}}{{r}}"
        )
        fileAppender(
            pattern = "[{{level}}][{{yyyy}}/{{MM}}/{{dd}} {{hh}}:{{mm}}:{{ss}}.{{SSS}}] ({{name}} @ {{thread}}) {{message}}",
            path = "latest.log",
            filter = Filter.levelsExcept(Level.DEBUG, Level.TRACE)
        )
        fileAppender(
            pattern = "[{{level}}][{{yyyy}}/{{MM}}/{{dd}} {{hh}}:{{mm}}:{{ss}}.{{SSS}}] ({{name}} @ {{thread}}) {{message}}",
            path = "debug.log",
            filter = Filter.levels(Level.DEBUG)
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

    @OptIn(ExperimentalTime::class)
    override val defaultFormatter: Formatter get() = SimpleFormatter.default

    override fun addShutdownHook(hook: () -> Unit) = ShutdownHandler.register(hook)

    override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker {
        return DefaultMarker(key, name, isEnabled)
    }

    override fun createLogger(name: String, contextSpec: ContextSpec): Logger {
        val config = ConfigBuilder().apply(configSpec).build()
        // @formatter:off
        val context = ContextBuilder()
            .apply(this.contextSpec)
            .apply(contextSpec)
            .build()
        // @formatter:on
        return DefaultLogger(name, config, context)
    }
}