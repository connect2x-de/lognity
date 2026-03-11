package de.connect2x.lognity.config.override

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.condition.MarkerConditionType
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Condition that matches against a [Marker].
 *
 * @property condition The comparison type to apply.
 * @property value The string value to compare against.
 */
@SerialName("marker")
@Serializable
data class MarkerOverrideCondition( // @formatter:off
    val condition: RefOrValue<MarkerConditionType>,
    val value: RefOrValue<String>
) : AbstractSerializableOverrideCondition() { // @formatter:on
    override operator fun invoke(logger: Logger, level: Level, marker: Marker?): Boolean {
        val value = this.value.resolve()
        return condition.resolve()(marker?.key, marker?.name, value)
    }
}