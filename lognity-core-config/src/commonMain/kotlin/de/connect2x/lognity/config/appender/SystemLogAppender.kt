package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * System log appender configuration.
 *
 * @property pattern a name/pattern used by the backend to group or identify the appender
 * @property formatter the identifier of the formatter to use (must exist in the formatter map)
 * @property filter optional filter that must pass for messages to be logged
 * @property name optional unique name for the appender
 */
@Serializable
@SerialName("system_log")
data class SystemLogAppender(
    override val pattern: String,
    override val formatter: String,
    override val filter: SerializableFilter = SerializableFilter(),
    override val name: String? = null
) : SerializableAppender