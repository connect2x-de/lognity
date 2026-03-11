package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * Functional interface for defining conditions for log config overrides.
 */
fun interface OverrideCondition {
    companion object {
        /**
         * A condition that is never met.
         */
        val never: OverrideCondition = { _, _, _ -> false }
    }

    /**
     * Determines whether the condition is met for the given [logger] and [marker].
     *
     * @param logger the logger to check.
     * @param level The level of the message being logged.
     * @param marker the marker to check.
     * @return true if the condition is met, false otherwise.
     */
    operator fun invoke(logger: Logger, level: Level, marker: Marker?): Boolean
}