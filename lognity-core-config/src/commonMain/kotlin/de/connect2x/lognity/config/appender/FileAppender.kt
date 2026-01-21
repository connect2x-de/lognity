package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * File appender configuration.
 *
 * Writes formatted log messages to the file at [path].
 *
 * @property pattern a name/pattern used by the backend to group or identify the appender
 * @property formatter the identifier of the formatter to use (must exist in the formatter map)
 * @property path target file path where logs are written
 * @property filter optional filter that must pass for messages to be logged
 */
@Serializable
@SerialName("file")
data class FileAppender(
    override val pattern: RefOrValue<String>,
    override val formatter: RefOrValue<String>,
    val path: RefOrValue<String>,
    override val filter: RefOrValue<SerializableFilter> = RefOrValue.Value(SerializableFilter()),
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : SerializableAppender