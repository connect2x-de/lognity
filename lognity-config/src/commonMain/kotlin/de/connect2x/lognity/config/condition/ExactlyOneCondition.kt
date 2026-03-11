package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Composite condition which combines all of its sub-conditions and
 * only evaluates to true if exactly one of the conditions matches.
 */
@SerialName("exactly_one")
@Serializable
data class ExactlyOneCondition(
    val conditions: List<RefOrValue<SerializableCondition>>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() {
    override fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean { // @formatter:off
        return conditions.map { refOrValue -> refOrValue.resolveTemplate(config) }
            .count { condition -> condition(logger, level, message, marker) } == 1
    } // @formatter:on
}