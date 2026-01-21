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
import de.connect2x.lognity.logger.DefaultLogger
import de.connect2x.lognity.logger.DefaultMarker
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.concurrent.atomics.AtomicBoolean
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
    name: String?,
    deleteExisting: Boolean
): Appender // @formatter:on

internal expect fun createSystemRollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?,
    fileCount: Int,
    maxFileSize: Long,
    useTimestamps: Boolean,
    deleteExisting: Boolean,
    latestSuffix: String
): Appender // @formatter:on

/**
 * The default implementation of the Lognity [Backend].
 *
 * This object manages the lifecycle of the logging system, including:
 * - Providing the default [Level] and [Formatter].
 * - Handling [ConfigSpec] and [ContextSpec].
 * - Creating [Logger] and [Marker] instances.
 * - Managing a [CoroutineScope] for asynchronous operations like file writing.
 * - Registering shutdown hooks to ensure proper cleanup.
 */
@OptIn(ExperimentalAtomicApi::class)
object DefaultBackend : Backend {
    override val name: String = "Lognity"
    override val defaultLevel: Level = getDefaultLogLevel()

    private val isCoroutineScopeProviderSet: AtomicBoolean = AtomicBoolean(false)
    private val coroutineScopeProvider: AtomicReference<() -> CoroutineScope> = AtomicReference {
        val supervisorJob = SupervisorJob()
        ShutdownHandler.register(supervisorJob::cancel, priority = 100)
        CoroutineScope(Dispatchers.Default + supervisorJob + CoroutineName("Lognity"))
    }

    override val coroutineScope: CoroutineScope by lazy( // @formatter:off
        mode = LazyThreadSafetyMode.SYNCHRONIZED,
        initializer = coroutineScopeProvider.load()
    ) // @formatter:on

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
        check(!isCoroutineScopeProviderSet.compareAndExchange(expectedValue = false, newValue = true)) {
            "CoroutineScope provider was already set for logging backend"
        }
        coroutineScopeProvider.store(provider)
    }
}