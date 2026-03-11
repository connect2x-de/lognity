package de.connect2x.lognity.config

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.condition.AlwaysCondition
import de.connect2x.lognity.config.condition.SerializableCondition
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Serializable filter that decides whether a log entry should be passed to an appender.
 *
 * The filter holds a list of [conditions]; all conditions must evaluate to true for
 * a message to be accepted (logical AND).
 *
 * @property conditions list of conditions evaluated in order; defaults to a single [AlwaysCondition].
 */
@Serializable
data class SerializableFilter(
    val conditions: List<RefOrValue<SerializableCondition>> = listOf(RefOrValue.Value(AlwaysCondition))
) : Filter {
    @Transient
    private lateinit var _config: SerializableConfig
    var config: SerializableConfig
        get() = _config
        set(value) {
            for (condition in conditions) { // Propagate to conditions
                condition.resolveTemplate(value).config = value
            }
            _config = value
        }

    override operator fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean {
        return conditions.all { cond -> cond.resolveTemplate(config)(logger, level, message, marker) }
    }
}