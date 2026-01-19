package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.config.ConfigSpec
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.Logger.Name
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.systemLogAppender
import de.connect2x.lognity.format.SimpleFormatter
import de.connect2x.lognity.io.SinkCache
import de.connect2x.lognity.logger.DefaultLogger
import de.connect2x.lognity.logger.DefaultMarker
import de.connect2x.lognity.util.cancelAndJoinBlocking
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal expect fun getDefaultLogLevel(): Level

internal expect fun createSystemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender // @formatter:on

internal expect fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender // @formatter:on

internal expect fun createSystemFileAppender( // @formatter:off
    path: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender // @formatter:on

internal expect fun createSystemRollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?,
    fileCount: Int,
    maxFileSize: Long
): Appender // @formatter:on

@OptIn(ExperimentalAtomicApi::class)
object DefaultBackend : Backend {
    override val name: String = "Lognity"
    override val defaultLevel: Level = getDefaultLogLevel()

    internal val sinkCache: SinkCache = SinkCache()
    private val supervisorJob: Job = SupervisorJob()

    private val coroutineScopeProvider: AtomicReference<() -> CoroutineScope> = AtomicReference {
        CoroutineScope(Dispatchers.Default + supervisorJob + CoroutineName("Lognity"))
    }

    override val coroutineScope: CoroutineScope by lazy( // @formatter:off
        mode = LazyThreadSafetyMode.SYNCHRONIZED,
        initializer = coroutineScopeProvider.load()
    ) // @formatter:on

    init {
        ShutdownHandler.register(::onShutdown, priority = 100) // This needs to be run last
    }

    private val _configSpec: AtomicReference<ConfigSpec> = AtomicReference {
        systemLogAppender(
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

    private fun onShutdown() {
        supervisorJob.cancelAndJoinBlocking()
    }

    override fun addShutdownHook(hook: () -> Unit) = ShutdownHandler.register(hook)

    override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker {
        return DefaultMarker(key, name, isEnabled)
    }

    override fun createLogger(name: String?, contextSpec: ContextSpec): Logger {
        return DefaultLogger(Config(configSpec), Context {
            this@DefaultBackend.contextSpec(this)
            contextSpec()
            name?.let(::Name)?.let(::value)
        })
    }

    override fun setCoroutineScopeProvider(provider: () -> CoroutineScope) {
        coroutineScopeProvider.store(provider)
    }
}