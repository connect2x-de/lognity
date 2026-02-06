package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
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
    val condition: RefOrValue<Type>,
    val value: RefOrValue<String>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() { // @formatter:on
    enum class Type { EQUALS, NOT_EQUALS, CONTAINS, NOT_CONTAINS }

    override operator fun invoke(logger: Logger, message: String, marker: Marker?): Boolean {
        val value = this.value.resolve()
        return when (condition.resolve()) {
            Type.EQUALS -> message == value
            Type.NOT_EQUALS -> message != value
            Type.CONTAINS -> value in message
            Type.NOT_CONTAINS -> value !in message
        }
    }
}