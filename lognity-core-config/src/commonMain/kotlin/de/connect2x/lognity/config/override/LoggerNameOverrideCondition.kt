package de.connect2x.lognity.config.override

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.condition.StringConditionType
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Condition that matches against a logger's name.
 *
 * @property condition The comparison type to apply.
 * @property value The string value to compare against.
 */
@SerialName("logger_name")
@Serializable
data class LoggerNameOverrideCondition( // @formatter:off
    val condition: RefOrValue<StringConditionType>,
    val value: RefOrValue<String>
) : AbstractSerializableOverrideCondition() { // @formatter:on
    override fun invoke(logger: Logger, level: Level, marker: Marker?): Boolean {
        val name = logger.context[Logger.Name]?.name ?: return false
        val value = this.value.resolve()
        return condition.resolve()(name, value)
    }
}