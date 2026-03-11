package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Filters by the coroutine name in the logger context.
 *
 * @property condition how to evaluate the [value] against the coroutine name
 * @property value string to compare with the coroutine name
 * @property name optional name of the condition
 */
@Serializable
@SerialName("coroutine_name")
data class CoroutineNameCondition( // @formatter:off
    val condition: RefOrValue<StringConditionType>,
    val value: RefOrValue<String>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() { // @formatter:on
    override fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean {
        val name = logger.context[Logger.CoroutineName]?.name ?: return false
        val value = this.value.resolve()
        return condition.resolve()(name, value)
    }
}