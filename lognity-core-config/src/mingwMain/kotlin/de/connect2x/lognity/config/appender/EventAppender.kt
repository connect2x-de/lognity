package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable configuration for a Windows Event Log appender.
 *
 * @property pattern The formatting pattern string.
 * @property formatter The name of the formatter to use.
 * @property filter The filter configuration.
 * @property name Optional name for this appender.
 */
@Serializable
@SerialName("winevent")
data class EventAppender(
    override val pattern: RefOrValue<String>,
    override val formatter: RefOrValue<String>,
    override val filter: RefOrValue<SerializableFilter> = RefOrValue.Value(SerializableFilter()),
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : SerializableAppender