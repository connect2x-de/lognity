package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("system_console")
data class SystemConsoleAppender(
    override val pattern: RefOrValue<String>,
    override val formatter: RefOrValue<String>,
    override val filter: RefOrValue<SerializableFilter> = RefOrValue.Value(SerializableFilter()),
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : SerializableAppender