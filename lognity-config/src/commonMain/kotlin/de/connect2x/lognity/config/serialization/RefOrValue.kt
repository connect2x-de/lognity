package de.connect2x.lognity.config.serialization

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Serializable

@Serializable(with = RefOrValueSerializer::class)
sealed interface RefOrValue<out T> {
    data class Ref<out T>(val name: String) : RefOrValue<T> {
        override fun resolve(): T {
            return checkNotNull(SerializableConfig.extensionRegistrar.findProvider<T>(name)) {
                "Could not resolve provider for config reference '$name'"
            }.value
        }
    }

    data class Value<out T>(val value: T) : RefOrValue<T> {
        override fun resolve(): T = value
    }

    fun resolve(): T
}