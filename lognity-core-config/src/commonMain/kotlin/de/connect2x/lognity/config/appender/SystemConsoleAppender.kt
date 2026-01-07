package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("system_console")
data class SystemConsoleAppender(
    override val pattern: String,
    override val formatter: String,
    override val filter: SerializableFilter = SerializableFilter(),
    override val name: String? = null
) : SerializableAppender {}