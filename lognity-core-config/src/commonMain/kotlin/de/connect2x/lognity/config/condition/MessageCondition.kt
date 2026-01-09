package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Filters by the content of the log message string.
 *
 * @property condition how to evaluate the [value] against the message
 * @property value string to compare with the message
 */
@SerialName("message")
@Serializable
data class MessageCondition( // @formatter:off
    val condition: Type,
    val value: String,
    override val name: String? = null
) : SerializableCondition { // @formatter:on
    enum class Type { EQUALS, NOT_EQUALS, CONTAINS, NOT_CONTAINS }

    override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
        return when (condition) {
            Type.EQUALS -> message == value
            Type.NOT_EQUALS -> message != value
            Type.CONTAINS -> value in message
            Type.NOT_CONTAINS -> value !in message
        }
    }
}