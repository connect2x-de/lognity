package de.connect2x.lognity.api.format

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * A function which represents a transformation applied for a given template variable
 * in the log pattern.
 */
@Suppress("NOTHING_TO_INLINE")
fun interface Formatter {
    companion object {
        /**
         * A formatter that returns the input string unchanged.
         * This identity formatter can be used as a base for building more complex formatters
         * or when no formatting is desired.
         */
        val identity: Formatter = Formatter { _, _, _, _, s -> s }

        /**
         * The default formatter provided by the current log backend.
         */
        inline val default: Formatter get() = Backend.defaultFormatter
    }

    /**
     * Transforms the given string and replaces all occurrences
     * of the template variable associated with this format element.
     *
     * @param logger The logger instance associated with this format element.
     * @param level The level at which the message will be logged.
     * @param content The raw content of the message.
     * @param marker The log marker the message being formatted is tagged with.
     * @param s The string being transformed.
     * @return The transformed string or the original string if no template variable was replaced.
     */
    operator fun invoke( // @formatter:off
        logger: Logger,
        level: Level,
        content: Any,
        marker: Marker?,
        s: String
    ): String // @formatter:on

    /**
     * Concatenates this format element with another to form a new [Formatter] instance.
     *
     * @param other The element with which to join this element to form a new formatter instance.
     * @return A new [Formatter] instance containing both this and the other format element.
     */
    operator fun plus(other: Formatter): Formatter = Formatter { logger, level, content, marker, s ->
        other(logger, level, content, marker, this(logger, level, content, marker, s))
    }
}