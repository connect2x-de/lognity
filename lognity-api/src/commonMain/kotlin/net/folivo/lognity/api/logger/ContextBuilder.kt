package net.folivo.lognity.api.logger

/**
 * Builder for creating immutable [Context] instances.
 *
 * The builder collects key/value pairs and finally produces a [Context] via [build].
 * Use it together with [ContextSpec] or a DSL-like block when configuring a [Logger].
 */
class ContextBuilder {
    @PublishedApi
    internal val values: HashMap<Context.Key<*>, Any> = HashMap()

    /**
     * Adds all values contained in the given [context] to this builder.
     *
     * Existing entries with the same key will be overwritten by the values from [context].
     *
     * @param context The source context whose values should be copied into this builder.
     */
    fun valuesFrom(context: Context) {
        values += context.values
    }

    /**
     * Adds all provided [values] to this builder.
     *
     * Existing entries with the same key are overwritten by the provided values.
     *
     * @param values A map of [Context.Key] to value that will be merged into this builder.
     */
    fun values(values: Map<Context.Key<*>, Any>) {
        this.values += values
    }

    /**
     * Adds a single [value] addressed by a typed [key].
     *
     * @param key The strongly typed key under which the value is stored.
     * @param value The value to store.
     * @param T The type associated with the [key].
     */
    fun <T : Any> value(key: Context.Key<T>, value: T) {
        values[key] = value
    }

    /**
     * Adds a single [value] using a key identified by its [name] and inferred type [T].
     *
     * Convenience overload that internally creates a [Context.Key] via [Context.Key.create].
     *
     * @param name The name of the key under which the value is stored.
     * @param value The value to store.
     * @param T The type of the value, inferred from the call site.
     */
    inline fun <reified T : Any> value(name: String, value: T) {
        values[Context.Key.create<T>(name)] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: Context.Key<T>): T? = values[key] as? T

    operator fun <T : Any> set(key: Context.Key<T>, value: T) {
        values[key] = value
    }

    /**
     * Builds an immutable [Context] from the values currently stored in this builder.
     *
     * @return A new [Context] instance containing all collected entries.
     */
    fun build(): Context = Context(values)
}

/**
 * A specification block used to configure a [ContextBuilder] in a DSL-like manner.
 *
 * Example:
 * `val ctx = ContextBuilder().apply { value("requestId", "123") }.build()`
 */
typealias ContextSpec = ContextBuilder.() -> Unit