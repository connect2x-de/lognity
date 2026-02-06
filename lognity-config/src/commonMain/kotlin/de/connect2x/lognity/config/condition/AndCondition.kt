package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Composite condition which conjunctively combines all of its sub-conditions.
 */
@SerialName("and")
@Serializable
data class AndCondition(
    val conditions: List<RefOrValue<SerializableCondition>>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() {
    override fun invoke(logger: Logger, message: String, marker: Marker?): Boolean {
        return conditions.all { refOrValue -> refOrValue.resolveTemplate(config)(logger, message, marker) }
    }
}