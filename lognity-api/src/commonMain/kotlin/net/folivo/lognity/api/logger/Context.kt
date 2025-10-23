package net.folivo.lognity.api.logger

import kotlin.reflect.KClass

/**
 * Immutable, type-safe key/value container used to attach additional information to [Logger] instances.
 *
 * Entries are addressed by [Key] instances which carry both a human-readable [Key.name] and the
 * value [Key.type]. This enables safe retrieval without unchecked casts at call sites.
 *
 * Contexts can be combined using the plus operator: `left + right` returns a new Context where
 * values from the right-hand side override values from the left when keys collide.
 *
 * This class is safe to share across threads. It does not mutate its internal state; combining
 * contexts creates new instances.
 */
data class Context(
    internal val values: Map<Key<*>, Any> = HashMap()
) {
    /**
     * Strongly-typed key used to store and retrieve values from a [Context].
     *
     * @param name The name of the context value.
     * @param type The reflection type of the context value.
     * @param T The type of the context value associated with this key.
     */
    data class Key<T : Any>( // @formatter:off
        val name: String,
        val type: KClass<T>
    ) { // @formatter:on
        companion object {
            /**
             * Creates a typed [Key] for values of type [T].
             *
             * @param name The name of the context value.
             * @param T The type of the context value associated with the created key.
             * @return A new [Key] instance with [name] and type [T].
             */
            inline fun <reified T : Any> create(name: String): Key<T> = Key(name, T::class)
        }
    }

    /**
     * Returns the value associated with the given [key] or null if it is not present.
     *
     * @param key The strongly typed key of the context value to retrieve.
     * @param T The type of the context value associated with the given key.
     * @return The context value of type [T] if present, otherwise `null`.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: Key<T>): T? = values[key] as? T

    /**
     * Returns a new [Context] that contains all entries from this context and [other].
     * If both contain the same [Key], the value from [other] wins.
     *
     * @param other The context with which to combine this context.
     * @return A new immutable context instance with all duplicate keys overwritten by [other].
     */
    operator fun plus(other: Context): Context = Context(values + other.values)
}