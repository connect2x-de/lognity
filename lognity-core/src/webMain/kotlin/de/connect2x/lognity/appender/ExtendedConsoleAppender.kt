package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.withBlockingLock
import kotlin.js.JsName

internal external interface Console {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}

@JsName("console")
internal external val console: Console

class ExtendedConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
) : ConsoleAppender(pattern, formatter, filter) { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        mutex.withBlockingLock {
            when (level) {
                Level.DEBUG, Level.TRACE -> console.debug(message)
                Level.INFO -> console.info(message)
                Level.WARN -> console.warn(message)
                Level.ERROR, Level.FATAL -> console.error(message)
            }
        }
    }
}