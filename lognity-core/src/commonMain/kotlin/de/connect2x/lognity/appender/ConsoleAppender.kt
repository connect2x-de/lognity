package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * Appender that prints formatted log messages to the standard output.
 *
 * @param pattern Defines the format template interpreted by the provided [formatter].
 * @param formatter Renders the final message string from a log event.
 * @param filter Decides whether a log event should be emitted at all.
 * @property name Optional name for this appender.
 */
open class ConsoleAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    override val name: String? = null
) : AbstractAppender() { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        println(message)
    }
}