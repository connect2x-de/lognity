package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Condition that always evaluates to true.
 */
@SerialName("always")
@Serializable
data object AlwaysCondition : SerializableCondition {
    override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
        return true
    }
}