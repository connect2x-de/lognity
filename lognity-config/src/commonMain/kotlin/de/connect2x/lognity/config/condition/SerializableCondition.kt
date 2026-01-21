package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.Polymorphic

/**
 * Base type for all filter conditions used by [de.connect2x.lognity.config.SerializableFilter].
 *
 * Implementations decide based on the log [Level], message text, and optional [Marker].
 */
@Polymorphic
interface SerializableCondition {
    /**
     * Optional name for this condition.
     */
    val name: RefOrValue<String?>

    /**
     * Evaluates the condition.
     *
     * @param level the log level.
     * @param message the log message.
     * @param marker the log marker.
     * @return true if the condition is met, false otherwise.
     */
    operator fun invoke(level: Level, message: String, marker: Marker?): Boolean
}