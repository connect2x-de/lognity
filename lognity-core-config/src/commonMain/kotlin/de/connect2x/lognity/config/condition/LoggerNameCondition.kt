package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Filters by the name in the logger context.
 *
 * @property condition how to evaluate the [value] against the name
 * @property value string to compare with the name
 * @property name optional name of the condition
 */
@Serializable
@SerialName("logger_name")
data class LoggerNameCondition( // @formatter:off
    val condition: RefOrValue<Type>,
    val value: RefOrValue<String>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() { // @formatter:on
    enum class Type { EQUALS, NOT_EQUALS, CONTAINS, NOT_CONTAINS }

    override fun invoke(logger: Logger, message: String, marker: Marker?): Boolean {
        val name = logger.context[Logger.Name]?.name ?: return false
        val value = this.value.resolve()
        return when (condition.resolve()) {
            Type.EQUALS -> name == value
            Type.NOT_EQUALS -> name != value
            Type.CONTAINS -> value in name
            Type.NOT_CONTAINS -> value !in name
        }
    }
}