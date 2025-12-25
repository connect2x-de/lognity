package de.connect2x.lognity.api.appender

import de.connect2x.lognity.api.appender.Filter.Companion.levels
import de.connect2x.lognity.api.appender.Filter.Companion.markers
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker

/**
 * A functional interface which allows expressing a finely grained
 * message filter which can be applied on a per-appender basis.
 */
@Suppress("NOTHING_TO_INLINE")
fun interface Filter {
    companion object {
        /**
         * A filter which will always let all messages through.
         */
        val always: Filter = Filter { _, _, _ -> true }

        /**
         * Creates a filter which lets through all messages at the specified level(s).
         *
         * @param levels The levels at which to pass messages.
         * @return A new filter instance only allowing the given levels.
         */
        inline fun levels(vararg levels: Level): Filter = object : Filter {
            private val filteredLevels: Set<Level> = levels.toSet()
            override fun invoke(level: Level, message: String, marker: Marker?): Boolean = level in filteredLevels
        }

        /**
         * Creates a filter which lets through all messages at any level except the specified ones.
         *
         * @param levels The levels at which to omit messages.
         * @return A new filter instance allowing any level except the given ones.
         */
        inline fun levelsExcept(vararg levels: Level): Filter = object : Filter {
            private val filteredLevels: Set<Level> = Level.entries.toSet() - levels.toSet()
            override fun invoke(level: Level, message: String, marker: Marker?): Boolean = level in filteredLevels
        }

        /**
         * Creates a filter which lets through all messages with the given marker if enabled.
         *
         * @param markers The markers to pass messages for.
         * @return A new filter instance allowing all messages with the given markers to pass.
         */
        inline fun markers(vararg markers: Marker): Filter = object : Filter {
            private val filteredMarkers: Set<Marker> = markers.toSet()
            override fun invoke(level: Level, message: String, marker: Marker?): Boolean = marker in filteredMarkers
        }

        /**
         * Creates a filter which lets through only messages containing the given substring.
         *
         * @param s The substring to look for in all messages-
         * @return A new filter instance allowing all messages with the given substring to pass.
         */
        inline fun containsString(s: String): Filter = Filter { _, message, _ -> s in message }
    }

    /**
     * Check if the given message should be passed to the appender this filter is associated with.
     *
     * @param level The current log level.
     * @param message The formatted message.
     * @param marker The marker of the current message.
     * @return True if the current message should be forwarded to the appender.
     */
    operator fun invoke(level: Level, message: String, marker: Marker?): Boolean

    /**
     * Combines this filter with another filter using a logical AND operation.
     *
     * @param other The other filter to combine with.
     * @return A new filter which only lets messages through if both filters allow it.
     */
    infix fun and(other: Filter): Filter = Filter { level, message, marker ->
        this(level, message, marker) && other(level, message, marker)
    }

    /**
     * Combines this filter with another filter using a logical OR operation.
     *
     * @param other The other filter to combine with.
     * @return A new filter which lets messages through if at least one of the filters allows it.
     */
    infix fun or(other: Filter): Filter = Filter { level, message, marker ->
        this(level, message, marker) || other(level, message, marker)
    }

    /**
     * Negates the result of this filter.
     *
     * @return A new filter which lets messages through if this filter does not.
     */
    fun not(): Filter = Filter { level, message, marker -> !this(level, message, marker) }
}