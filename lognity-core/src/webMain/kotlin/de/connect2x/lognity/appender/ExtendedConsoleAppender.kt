package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.isChrome
import de.connect2x.lognity.util.isNode
import kotlin.js.JsName

internal external interface Console {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}

@JsName("console")
internal external val console: Console

/**
 * A console appender for web that leverages the JavaScript console's log levels.
 *
 * This appender maps [Level]s to their corresponding `console` methods:
 * - [Level.DEBUG] and [Level.TRACE] -> `console.debug`
 * - [Level.INFO] -> `console.info`
 * - [Level.WARN] -> `console.warn`
 * - [Level.ERROR] and [Level.FATAL] -> `console.error`
 *
 * It also handles ANSI escape codes, preserving them for Chrome and Node.js environments
 * while cleaning them for others.
 *
 * @param pattern The formatting pattern string used by this appender.
 * @param formatter The formatter that produces the final message from [pattern].
 * @param filter A filter that decides whether a given message should be written.
 * @param name Optional name for this appender.
 */
class ExtendedConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String? = null
) : ConsoleAppender(pattern, formatter, filter, name) { // @formatter:on
    companion object {
        private val messageProcessor: (String) -> String = when {
            isChrome || isNode -> { message -> message }
            else -> { message -> message.toAnsi().cleanString() }
        }
    }

    override fun append(
        logger: Logger, level: Level, message: String, marker: Marker?
    ) {
        if (level < logger.level || !filter(logger, message, marker)) return
        // Only Chrome supports ANSI escape codes in the JS console right now
        val processedMessage = messageProcessor(message)
        when (level) {
            Level.DEBUG, Level.TRACE -> console.debug(processedMessage)
            Level.INFO -> console.info(processedMessage)
            Level.WARN -> console.warn(processedMessage)
            Level.ERROR, Level.FATAL -> console.error(processedMessage)
        }
    }
}