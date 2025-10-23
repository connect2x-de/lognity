package net.folivo.lognity.config

import kotlinx.serialization.Serializable
import net.folivo.lognity.api.logger.Level

@Serializable
internal data class SerializableConfig( // @formatter:off
    val version: Int = VERSION,
    val level: Level = Level.default(),
    val enabled: Boolean = true,
    val appenders: List<SerializableAppender> = emptyList()
) { // @formatter:on
    companion object {
        const val VERSION: Int = 1
    }
}