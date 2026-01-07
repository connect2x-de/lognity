package de.connect2x.lognity.api.appender

import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * Interface representing a log appender, which is responsible for outputting log messages
 * to a specific destination (e.g., console, file, network, etc.).
 *
 * Appenders receive formatted log messages and write them to their respective output targets.
 * Each appender has a formatter that controls how messages are formatted before being appended,
 * and typically works with a filter to determine which messages should be processed.
 */
interface Appender {
    /**
     * The internal name of this appender.
     * This is the name used when referencing appenders in the JSON configuration.
     */
    val name: String?

    /**
     * The formatter used by this appender to transform log messages according to a pattern.
     * The formatter applies the pattern to the raw message content before it is appended.
     */
    val formatter: Formatter

    /**
     * The formatting pattern string used by this appender.
     * This pattern defines how log messages will be structured when output to the destination.
     * The pattern is processed by the formatter to produce the final formatted message.
     */
    val pattern: String

    /**
     * The filter applied to all messages passed into this appender instance.
     * This should drop any unwanted messages from actually being written by this appender.
     */
    val filter: Filter get() = Filter.always

    /**
     * Appends a log message to the appender's destination.
     *
     * @param logger The logger instance that generated this log message.
     * @param level The log level of the message.
     * @param message The formatted message content to be appended.
     * @param marker An optional marker that can be used for additional filtering or processing.
     */
    fun append(logger: Logger, level: Level, message: String, marker: Marker?)

    /**
     * Allows flushing the underlying IO of this appender implementation if present.
     * Will be invoked when calling [Logger.flush].
     */
    fun flush() {}
}
