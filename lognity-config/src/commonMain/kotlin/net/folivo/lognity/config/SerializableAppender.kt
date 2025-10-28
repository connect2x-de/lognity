package net.folivo.lognity.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
@Polymorphic
sealed interface SerializableAppender {
    companion object {
        val serializersModule: SerializersModule = SerializersModule {
            polymorphic(SerializableAppender::class) {
                subclass(Console::class)
                subclass(File::class)
            }
        }
    }

    @Serializable
    @SerialName("console")
    data class Console( // @formatter:off
        val pattern: String,
        val formatter: String,
        val filter: SerializableFilter = SerializableFilter()
    ) : SerializableAppender // @formatter:on

    @Serializable
    @SerialName("file")
    data class File(
        val pattern: String,
        val formatter: String,
        val path: String,
        val filter: SerializableFilter = SerializableFilter()
    ) : SerializableAppender
}