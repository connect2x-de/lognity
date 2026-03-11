package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Composite condition which disjunctively combines all of its sub-conditions.
 */
@SerialName("or")
@Serializable
data class OrCondition(
    val conditions: List<RefOrValue<SerializableCondition>>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() {
    override fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean {
        return conditions.any { refOrValue -> refOrValue.resolveTemplate(config)(logger, level, message, marker) }
    }
}