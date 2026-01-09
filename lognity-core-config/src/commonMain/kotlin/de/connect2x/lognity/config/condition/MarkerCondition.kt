package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
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
    val condition: Type,
    val value: String,
    override val name: String? = null
) : SerializableCondition { // @formatter:on
    enum class Type {
        KEY_EQUALS, KEY_NOT_EQUALS, KEY_CONTAINS, KEY_NOT_CONTAINS, NAME_EQUALS, NAME_NOT_EQUALS, NAME_CONTAINS, NAME_NOT_CONTAINS
    }

    override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
        return when (condition) {
            Type.KEY_EQUALS -> marker?.key == value
            Type.KEY_NOT_EQUALS -> marker?.key != value
            Type.KEY_CONTAINS -> marker?.key?.let { key -> value in key } == true
            Type.KEY_NOT_CONTAINS -> marker?.key?.let { key -> value !in key } == true
            Type.NAME_EQUALS -> marker?.name == value
            Type.NAME_NOT_EQUALS -> marker?.name != value
            Type.NAME_CONTAINS -> marker?.name?.let { name -> value in name } == true
            Type.NAME_NOT_CONTAINS -> marker?.name?.let { name -> value !in name } == true
        }
    }
}