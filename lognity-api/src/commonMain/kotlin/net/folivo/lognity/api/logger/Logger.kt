package net.folivo.lognity.api.logger

import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.marker.Marker

/**
 * An interface which provides functions to log to all appenders of
 * this instance.
 */
interface Logger {
    companion object {
        /**
         * The default configuration specification used when creating new logger instances
         * without explicitly providing a configuration.
         *
         * This property delegates to the current [Backend]'s defaultConfigSpec.
         * Changing this value affects all subsequently created loggers that don't specify
         * their own configuration.
         *
         * @see ConfigBuilder
         */
        var defaultConfig: ConfigBuilder.() -> Unit
            get() = Backend.current.configSpec
            set(value) {
                Backend.current.configSpec = value
            }
    }

    /**
     * The name of this logger instance.
     * This value is expanded into {{name}} in the formatting pattern.
     */
    val name: String

    /**
     * The immutable configuration of this logger instance.
     */
    val config: Config

    /**
     * The immutable context of this logger instance.
     * Contains metadata as specified in the trailing closure of [Logger].
     */
    val context: Context

    /**
     * The current log level of this logger instance.
     * This may be changed at any time from any thread.
     */
    var level: Level

    /**
     * If true, this logger will forward all messages to its appenders,
     * otherwise all messages will be omitted for this logger instance.
     */
    var isEnabled: Boolean

    /**
     * A function which may be explicitly invoked to free all underlying
     * resources associated with this logger instance.
     * If not called explicitly, will be called automatically by the backend
     * at the end of the program lifecycle.
     */
    fun dispose() {}

    /**
     * Log a message at the given level to all appenders.
     * If the given level is less than [Logger.level], the message will be omitted.
     *
     * @param level The level at which to report the message.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun log(level: Level, message: AnsiScope.() -> Any)

    /**
     * Log a message at the given level to all appenders.
     * If the given level is less than [Logger.level], the message will be omitted.
     * Additionally, if the given marker is disabled, this message will be omitted as well.
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param level The level at which to report the message.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any)

    /**
     * Log a message at the [Level.TRACE] level if enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(LogLevel.TRACE, message)
     * ```
     *
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun trace(message: AnsiScope.() -> Any) = log(Level.TRACE, message)

    /**
     * Log a message at the [Level.DEBUG] level if enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(LogLevel.DEBUG, message)
     * ```
     *
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun debug(message: AnsiScope.() -> Any) = log(Level.DEBUG, message)

    /**
     * Log a message at the [Level.INFO] level if enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(LogLevel.INFO, message)
     * ```
     *
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun info(message: AnsiScope.() -> Any) = log(Level.INFO, message)

    /**
     * Log a message at the [Level.WARN] level if enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(LogLevel.WARN, message)
     * ```
     *
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun warn(message: AnsiScope.() -> Any) = log(Level.WARN, message)

    /**
     * Log a message at the [Level.ERROR] level if enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(LogLevel.ERROR, message)
     * ```
     *
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun error(message: AnsiScope.() -> Any) = log(Level.ERROR, message)

    /**
     * Log a message at the [Level.FATAL] level if enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(LogLevel.FATAL, message)
     * ```
     *
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun fatal(message: AnsiScope.() -> Any) = log(Level.FATAL, message)

    /**
     * Log a message at the [Level.TRACE] level and with the given marker if both are enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(marker, LogLevel.TRACE, message)
     * ```
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun trace(marker: Marker?, message: AnsiScope.() -> Any) = log(marker, Level.TRACE, message)

    /**
     * Log a message at the [Level.DEBUG] level and with the given marker if both are enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(marker, LogLevel.DEBUG, message)
     * ```
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun debug(marker: Marker?, message: AnsiScope.() -> Any) = log(marker, Level.DEBUG, message)

    /**
     * Log a message at the [Level.INFO] level and with the given marker if both are enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(marker, LogLevel.INFO, message)
     * ```
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun info(marker: Marker?, message: AnsiScope.() -> Any) = log(marker, Level.INFO, message)

    /**
     * Log a message at the [Level.WARN] level and with the given marker if both are enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(marker, LogLevel.WARN, message)
     * ```
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun warn(marker: Marker?, message: AnsiScope.() -> Any) = log(marker, Level.WARN, message)

    /**
     * Log a message at the [Level.ERROR] level and with the given marker if both are enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(marker, LogLevel.ERROR, message)
     * ```
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun error(marker: Marker?, message: AnsiScope.() -> Any) = log(marker, Level.ERROR, message)

    /**
     * Log a message at the [Level.FATAL] level and with the given marker if both are enabled.
     * Semantically equal to
     * ```kotlin
     * Logger.log(marker, LogLevel.FATAL, message)
     * ```
     *
     * @param marker The marker with which to tag the logged message if not null.
     * @param message An ANSI-string closure which returns any object whose [toString] function will be invoked.
     */
    fun fatal(marker: Marker?, message: AnsiScope.() -> Any) = log(marker, Level.FATAL, message)
}

/**
 * Create a new [Logger] instance with the given name.
 *
 * @param name The name of the newly created logger instance.
 * @param contextSpec The [Context] applied to the newly created logger instance.
 * @return A new logger instance with the given name.
 */
fun Logger(name: String, contextSpec: ContextSpec = {}): Logger {
    return Backend.current.createLogger(name, contextSpec)
}