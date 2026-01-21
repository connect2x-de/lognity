package de.connect2x.lognity.config.serialization

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Serializable

/**
 * A wrapper for a value that can either be provided directly or referenced by name.
 *
 * @param T the type of the value.
 */
@Serializable(with = RefOrValueSerializer::class)
sealed interface RefOrValue<out T> {
    /**
     * A reference to a value provided by a configuration provider.
     *
     * @property name the name of the reference.
     */
    data class Ref<out T>(val name: String) : RefOrValue<T> {
        override fun resolve(): T {
            return checkNotNull(SerializableConfig.extensionRegistrar.findProvider<T>(name)) {
                "Could not resolve provider for config reference '$name'"
            }()
        }
    }

    /**
     * A direct value.
     *
     * @property value the value.
     */
    data class Value<out T>(val value: T) : RefOrValue<T> {
        override fun resolve(): T = value
    }

    /**
     * A string that contains mixed literal segments and references.
     *
     * @property segments the segments of the string.
     */
    data class LerpedString(val segments: List<RefOrValue<*>>) : RefOrValue<String> {
        override fun resolve(): String = segments.joinToString("") { seg -> seg.resolve().toString() }
    }

    /**
     * Resolves the value.
     *
     * @return the resolved value.
     */
    fun resolve(): T
}