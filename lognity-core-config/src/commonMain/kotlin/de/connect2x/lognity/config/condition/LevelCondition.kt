package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Filters by comparing the log [Level] against a reference value.
 *
 * @property condition comparison to apply between the current level and [value]
 * @property value the reference log level
 */
@SerialName("level")
@Serializable
data class LevelCondition( // @formatter:off
    val condition: Type,
    val value: Level,
    override val name: String? = null
) : SerializableCondition { // @formatter:on
    enum class Type { EQUALS, NOT_EQUALS, BELOW, ABOVE }

    override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
        return when (condition) {
            Type.EQUALS -> value == level
            Type.NOT_EQUALS -> value != level
            Type.BELOW -> level < value
            Type.ABOVE -> level > value
        }
    }
}