package net.folivo.lognity.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.marker.Marker

@Serializable
data class SerializableFilter(
    val conditions: List<Condition> = listOf(AlwaysCondition)
) : Filter {
    override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
        return conditions.all { cond -> cond(level, message, marker) }
    }

    @Serializable
    @Polymorphic
    sealed interface Condition {
        companion object {
            val serializersModule: SerializersModule = SerializersModule {
                polymorphic(Condition::class) {
                    subclass(AlwaysCondition::class)
                    subclass(MarkerCondition::class)
                    subclass(MessageCondition::class)
                    subclass(LevelCondition::class)
                }
            }
        }

        operator fun invoke(level: Level, message: String, marker: Marker?): Boolean
    }

    @SerialName("always")
    @Serializable
    data object AlwaysCondition : Condition {
        override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
            return true
        }
    }

    @SerialName("marker")
    @Serializable
    data class MarkerCondition( // @formatter:off
        val condition: Type,
        val value: String
    ) : Condition { // @formatter:on
        enum class Type {
            KEY_EQUALS, KEY_NOT_EQUALS, KEY_CONTAINS, KEY_NOT_CONTAINS, NAME_EQUALS, NAME_NOT_EQUALS, NAME_CONTAINS, NAME_NOT_CONTAINS
        }

        override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
            return when (condition) {
                Type.KEY_EQUALS -> marker?.key == value
                Type.KEY_NOT_EQUALS -> marker?.key != value
                Type.KEY_CONTAINS -> marker?.key?.let { key -> value in key } == true
                Type.KEY_NOT_CONTAINS -> marker?.key?.let { key -> value !in key } == true
                Type.NAME_EQUALS -> marker?.name == value
                Type.NAME_NOT_EQUALS -> marker?.name != value
                Type.NAME_CONTAINS -> marker?.name?.let { name -> value in name } == true
                Type.NAME_NOT_CONTAINS -> marker?.name?.let { name -> value !in name } == true
            }
        }
    }

    @SerialName("message")
    @Serializable
    data class MessageCondition( // @formatter:off
        val condition: Type,
        val value: String
    ) : Condition { // @formatter:on
        enum class Type { EQUALS, NOT_EQUALS, CONTAINS, NOT_CONTAINS }

        override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
            return when (condition) {
                Type.EQUALS -> message == value
                Type.NOT_EQUALS -> message != value
                Type.CONTAINS -> value in message
                Type.NOT_CONTAINS -> value !in message
            }
        }
    }

    @SerialName("level")
    @Serializable
    data class LevelCondition( // @formatter:off
        val condition: Type,
        val value: Level
    ) : Condition { // @formatter:on
        enum class Type { EQUALS, NOT_EQUALS, BELOW, ABOVE }

        override operator fun invoke(level: Level, message: String, marker: Marker?): Boolean {
            return when (condition) {
                Type.EQUALS -> value == level
                Type.NOT_EQUALS -> value != level
                Type.BELOW -> level < value
                Type.ABOVE -> level > value
            }
        }
    }
}