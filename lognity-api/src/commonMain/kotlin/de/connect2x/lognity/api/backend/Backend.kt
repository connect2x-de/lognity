package de.connect2x.lognity.api.backend

import de.connect2x.lognity.api.config.ConfigSpec
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.TestOnly
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference

@PublishedApi
internal val currentBackend: AtomicReference<Backend> = AtomicReference(NoopBackend)

@PublishedApi
internal val isSetOnce: AtomicBoolean = AtomicBoolean(false)

/**
 * Interface representing a logging backend implementation.
 * The backend is responsible for creating loggers, markers,
 * as well as providing default configuration for the logging system.
 */
interface Backend {
    companion object : Backend {
        /**
         * Set the backend implementation used for logging.
         */
        fun set(backend: Backend) {
            // TODO: This should enforce being final, but this doesn't work on iOS right now due to a CAS bug..
            currentBackend.store(backend)
        }

        @Deprecated("Use set() directly as it has the same behaviour", replaceWith = ReplaceWith("set()"))
        @TestOnly
        inline fun <B : Backend> setOnce(backend: B, block: B.() -> Unit = {}) {
            if (!isSetOnce.compareAndSet(expectedValue = false, newValue = true)) return
            set(backend)
            backend.block()
        }

        @TestOnly
        fun reset() {
            isSetOnce.store(false)
            currentBackend.store(NoopBackend)
        }

        override val name: String get() = currentBackend.load().name
        override val defaultLevel: Level get() = currentBackend.load().defaultLevel
        override val overrideLevel: Level? get() = currentBackend.load().overrideLevel
        override val defaultFormatter: Formatter get() = currentBackend.load().defaultFormatter
        override val coroutineScope: CoroutineScope get() = currentBackend.load().coroutineScope
        override val consoleColorScheme: ConsoleColorScheme get() = currentBackend.load().consoleColorScheme

        override var configSpec: ConfigSpec
            get() = currentBackend.load().configSpec
            set(value) {
                currentBackend.load().configSpec = value
            }

        override var contextSpec: ContextSpec
            get() = currentBackend.load().contextSpec
            set(value) {
                currentBackend.load().contextSpec = value
            }

        override fun addShutdownHook(hook: () -> Unit) {
            currentBackend.load().addShutdownHook(hook)
        }

        override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker {
            return currentBackend.load().createMarker(key, name, isEnabled)
        }

        override fun createLogger(name: String?, contextSpec: ContextSpec): Logger {
            return currentBackend.load().createLogger(name, contextSpec)
        }

        override fun setCoroutineScopeProvider(provider: () -> CoroutineScope) {
            currentBackend.load().setCoroutineScopeProvider(provider)
        }
    }

    /**
     * The name of this logging backend implementation.
     */
    val name: String

    /**
     * The default log level used by this backend when not explicitly specified.
     * This is used as the initial level for new loggers.
     */
    val defaultLevel: Level

    /**
     * The global override log level as provided by the environment
     * through program arguments and environment variables.
     */
    val overrideLevel: Level?

    /**
     * The default log formatter used by this backend when not explicitly specified.
     * This is used for formatting log messages in appenders.
     */
    val defaultFormatter: Formatter

    /**
     * The default logger configuration specification used by this backend.
     * This is used when creating new loggers without an explicit configuration.
     */
    var configSpec: ConfigSpec

    /**
     * The default context specification applied to every logger created
     * through this backend.
     * The individual context specification of the [Logger] call is combined
     * with this spec to create the actual context of the new logger.
     */
    var contextSpec: ContextSpec

    /**
     * The coroutine scope used by the backend for asynchronous operations.
     */
    val coroutineScope: CoroutineScope

    /**
     * The global console color scheme as guessed by the Lognity implementation.
     * **Note that this might not be 100% accurate on all platforms!**
     */
    val consoleColorScheme: ConsoleColorScheme

    /**
     * Sets the provider for the coroutine scope used by the backend.
     *
     * @param provider A function that returns a [CoroutineScope].
     */
    fun setCoroutineScopeProvider(provider: () -> CoroutineScope)

    /**
     * Register the given shutdown hook to be invoked when the application terminates.
     *
     * @param hook The function to be invoked on shutdown.
     */
    fun addShutdownHook(hook: () -> Unit)

    /**
     * Creates a new log marker with the specified parameters.
     *
     * @param key The internal key the marker is referenced by in cache.
     * @param name The name of the marker that is printed when {{marker}} is used.
     * @param isEnabled When true, all messages with this marker will be logged.
     * @return A new log marker instance.
     */
    fun createMarker(key: String, name: String, isEnabled: Boolean): Marker

    /**
     * Creates a new logger with the specified name and context specification.
     *
     * @param name The name of the logger, typically following a hierarchical naming convention.
     * @param contextSpec The context specification for the logger.
     * @return A new logger instance.
     */
    fun createLogger(
        name: String? = null, contextSpec: ContextSpec = {}
    ): Logger
}
