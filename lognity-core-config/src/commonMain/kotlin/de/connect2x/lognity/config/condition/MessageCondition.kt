package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
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
    val condition: RefOrValue<StringConditionType>,
    val value: RefOrValue<String>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() { // @formatter:on
    override operator fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean {
        val value = this.value.resolve()
        return condition.resolve()(message, value)
    }
}