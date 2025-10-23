package net.folivo.lognity.appender

import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.backend.withBlockingLock

class ExtendedConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
) : ConsoleAppender(pattern, formatter, filter) { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        mutex.withBlockingLock {
            when (level) {
                Level.DEBUG, Level.TRACE -> console.log(message)
                Level.INFO -> console.info(message)
                Level.WARN -> console.warn(message)
                Level.ERROR, Level.FATAL -> console.error(message)
            }
        }
    }
}