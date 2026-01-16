package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

class ExtendedConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String? = null
) : ConsoleAppender(pattern, formatter, filter, name) { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level >= Level.ERROR) System.err.println(message)
        else println(message)
    }

    override fun flush() {
        System.out.flush()
        System.err.flush()
    }
}