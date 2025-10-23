package net.folivo.lognity.api.backend

import net.folivo.lognity.api.config.ConfigSpec
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.ContextSpec
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Interface representing a logging backend implementation.
 * The backend is responsible for creating loggers, markers,
 * as well as providing default configuration for the logging system.
 */
interface Backend {
    @OptIn(ExperimentalAtomicApi::class)
    companion object {
        private val _current: AtomicReference<Backend> = AtomicReference(NoopBackend)

        /**
         * The current active logging backend instance.
         * This property allows getting and setting the global logging backend.
         */
        var current: Backend
            get() = _current.load()
            set(value) {
                _current.store(value)
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
     * Register the given shutdown hook to be invoked when the application terminates.
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
     * Creates a new logger with the specified name and configuration.
     *
     * @param name The name of the logger, typically following a hierarchical naming convention.
     * @param configSpec The configuration specification for the logger. Defaults to the backend's default configuration.
     * @return A new logger instance.
     */
    fun createLogger(name: String, contextSpec: ContextSpec = {}): Logger
}
