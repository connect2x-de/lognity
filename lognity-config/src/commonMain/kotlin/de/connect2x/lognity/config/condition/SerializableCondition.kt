package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Transient

/**
 * Base type for all filter conditions used by [de.connect2x.lognity.config.SerializableFilter].
 *
 * Implementations decide based on the log [Level], message text, and optional [Marker].
 */
@Polymorphic
interface SerializableCondition {
    @Transient
    var config: SerializableConfig

    /**
     * Optional name for this condition.
     */
    val name: RefOrValue<String?>

    /**
     * Evaluates the condition.
     *
     * @param logger The current [Logger] instance.
     * @param message the log message.
     * @param marker the log marker.
     * @return true if the condition is met, false otherwise.
     */
    operator fun invoke(logger: Logger, message: String, marker: Marker?): Boolean
}