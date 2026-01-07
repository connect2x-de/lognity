package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
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
    override val pattern: String,
    override val formatter: String,
    val path: String,
    @SerialName("rolling") val isRolling: Boolean = false,
    override val filter: SerializableFilter = SerializableFilter(),
    override val name: String? = null
) : SerializableAppender