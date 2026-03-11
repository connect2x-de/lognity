package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Filters by properties of the optional Marker attached to the log entry.
 *
 * @property condition how to evaluate the [value] against marker key/name
 * @property value string to compare with marker key or name
 */
@SerialName("marker")
@Serializable
data class MarkerCondition( // @formatter:off
    val condition: RefOrValue<MarkerConditionType>,
    val value: RefOrValue<String>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() { // @formatter:on
    override operator fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean {
        val value = this.value.resolve()
        return condition.resolve()(marker?.key, marker?.name, value)
    }
}