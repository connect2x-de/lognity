package net.folivo.lognity.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
internal sealed interface SerializableAppender {
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