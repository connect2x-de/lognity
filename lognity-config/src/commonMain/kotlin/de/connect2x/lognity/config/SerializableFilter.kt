package de.connect2x.lognity.config

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.condition.AlwaysCondition
import de.connect2x.lognity.config.condition.SerializableCondition
import kotlinx.serialization.Serializable

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
    val conditions: List<SerializableCondition> = listOf(AlwaysCondition)
) : Filter {
    override operator fun invoke(logger: Logger, message: String, marker: Marker?): Boolean {
        return conditions.all { cond -> cond(logger.level, message, marker) }
    }
}