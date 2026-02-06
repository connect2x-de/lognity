package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
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
    val condition: RefOrValue<Type>,
    val value: RefOrValue<Level>,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() { // @formatter:on
    enum class Type { EQUALS, NOT_EQUALS, BELOW, ABOVE }

    override operator fun invoke(logger: Logger, message: String, marker: Marker?): Boolean {
        val value = this.value.resolve()
        return when (condition.resolve()) {
            Type.EQUALS -> value == logger.level
            Type.NOT_EQUALS -> value != logger.level
            Type.BELOW -> logger.level < value
            Type.ABOVE -> logger.level > value
        }
    }
}