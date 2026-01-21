package de.connect2x.lognity.config.serialization

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Serializable

@Serializable(with = RefOrValueSerializer::class)
sealed interface RefOrValue<out T> {
    data class Ref<out T>(val name: String) : RefOrValue<T> {
        override fun resolve(): T {
            return checkNotNull(SerializableConfig.extensionRegistrar.findProvider<T>(name)) {
                "Could not resolve provider for config reference '$name'"
            }()
        }
    }

    data class Value<out T>(val value: T) : RefOrValue<T> {
        override fun resolve(): T = value
    }

    data class LerpedString(val segments: List<RefOrValue<*>>) : RefOrValue<String> {
        override fun resolve(): String = segments.joinToString("") { seg -> seg.resolve().toString() }
    }

    fun resolve(): T
}