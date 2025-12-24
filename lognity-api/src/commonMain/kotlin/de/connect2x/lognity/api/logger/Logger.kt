package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.ansi.AnsiScope
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.marker.Marker

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
            get() = Backend.configSpec
            set(value) {
                Backend.configSpec = value
            }
    }

    /** The name of any given [Logger] instance */
    data class Name(val name: String) : Context.Element { // @formatter:off
        companion object : Context.Key<Name>
        override val key: Context.Key<*> = Name
    } // @formatter:on

    /** The default marker of all messages logged by a given [Logger] */
    data class DefaultMarker(val marker: Marker) : Context.Element { // @formatter:off
        companion object : Context.Key<DefaultMarker>
        override val key: Context.Key<*> = DefaultMarker
    } // @formatter:on

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

    /**
     * Flushed all appenders that are associated with this logger instance.
     * See [Appender.flush].
     */
    fun flush() {
        for (appender in config.appenders) {
            appender.flush()
        }
    }
}

/**
 * Create a new [Logger] instance with the given name.
 *
 * @param name The name of the newly created logger instance.
 * @param contextSpec The [Context] applied to the newly created logger instance.
 * @return A new logger instance with the given name.
 */
fun Logger(name: String? = null, contextSpec: ContextSpec = {}): Logger {
    return Backend.createLogger(name, contextSpec)
}

/**
 * Create a new Logger that derives from this instance.
 *
 * The derived logger keeps the same name as this logger (if any) and starts with a copy
 * of this logger's Context. The provided contextSpec is then applied on top, allowing you
 * to add or override context values for the derived instance without mutating the original.
 *
 * Typical use-cases are scoping loggers with request/operation specific metadata while
 * preserving the base logger's configuration and name.
 *
 * @param contextSpec A Context builder that augments the copied context for the derived logger.
 * @return A new Logger sharing the same name and configuration, with an extended Context.
 */
inline fun Logger.derive(crossinline contextSpec: ContextSpec): Logger {
    val name = context[Logger.Name]?.name
    return Backend.createLogger(name) {
        valuesFrom(context)
        contextSpec()
    }
}