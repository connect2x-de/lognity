package de.connect2x.lognity.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Serializable description of an appender used by Lognity.
 *
 * This is the polymorphic base type for all appender definitions that can
 * appear in the JSON configuration.
 */
@Serializable
@Polymorphic
sealed interface SerializableAppender {
    /**
     * Serializer module enabling polymorphic deserialization of SerializableAppender
     * subtypes from JSON.
     */
    companion object {
        val serializersModule: SerializersModule = SerializersModule {
            polymorphic(SerializableAppender::class) {
                subclass(Console::class)
                subclass(File::class)
            }
        }
    }

    /**
     * Console appender configuration.
     *
     * @property pattern a name/pattern used by the backend to group or identify the appender
     * @property formatter the identifier of the formatter to use (must exist in the formatter map)
     * @property filter optional filter that must pass for messages to be logged
     */
    @Serializable
    @SerialName("console")
    data class Console( // @formatter:off
        val pattern: String,
        val formatter: String,
        val filter: SerializableFilter = SerializableFilter()
    ) : SerializableAppender // @formatter:on

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
    data class File(
        val pattern: String,
        val formatter: String,
        val path: String,
        val filter: SerializableFilter = SerializableFilter()
    ) : SerializableAppender
}