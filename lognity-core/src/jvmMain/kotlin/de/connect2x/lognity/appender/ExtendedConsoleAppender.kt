package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * A console appender for JVM that differentiates between standard output and standard error.
 *
 * Messages with level [Level.ERROR] or higher are written to [System.err],
 * while other messages are written to [System.out].
 *
 * @property pattern The formatting pattern string used by this appender.
 * @property formatter The formatter that produces the final message from [pattern].
 * @property filter A filter that decides whether a given message should be written.
 * @property name Optional name for this appender.
 */
class ExtendedConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String? = null
) : ConsoleAppender(pattern, formatter, filter, name) { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || message.isEmpty() || !filter(logger, message, marker)) return
        if (level >= Level.ERROR) System.err.println(message)
        else println(message)
    }

    override fun flush() {
        System.out.flush()
        System.err.flush()
    }
}