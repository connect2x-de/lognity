package net.folivo.lognity.api.context

/**
 * Builder used to create a [Context] via a small DSL.
 *
 * The builder collects [Context.Element]s grouped by their [Context.Key] and builds an
 * immutable [Context] that can hold multiple elements per key. Duplicates (by equality)
 * are de-duplicated.
 */
class ContextBuilder @PublishedApi internal constructor() {
    private val values: HashMap<Context.Key<*>, HashSet<Context.Element>> = HashMap()

    /**
     * Copies all elements from the given [context] into this builder.
     *
     * Elements are merged by their [Context.Key].
     */
    fun valuesFrom(context: Context) {
        context.fold(Unit) { _, element ->
            values.getOrPut(element.key) { HashSet() } += element
        }
    }

    /**
     * Adds multiple elements grouped by key.
     *
     * If a key already exists in this builder, the given elements are merged into the
     * existing set and duplicates are removed.
     */
    fun values(values: Map<Context.Key<*>, Collection<Context.Element>>) {
        this.values += values.mapValues { (_, elements) -> elements.toHashSet() }
    }

    /**
     * Adds a single [value] for the given [key]. If [value] is null, the call is ignored.
     */
    fun <T : Context.Element?> value(key: Context.Key<T>, value: T) {
        values.getOrPut(key) { HashSet() } += value ?: return
    }

    /**
     * DSL setter alias for [value].
     */
    operator fun <T : Context.Element?> set(key: Context.Key<T>, value: T) = value(key, value)

    @PublishedApi
    internal fun build(): Context = DefaultContext(values)
}

/**
 * Type alias for a Context builder specification used by [context].
 */
typealias ContextSpec = ContextBuilder.() -> Unit

/**
 * Creates a new immutable [Context] using the provided [spec] DSL.
 *
 * Example:
 *
 * val ctx = context {
 *     value(UserIdKey, UserId("42"))
 * }
 */
inline fun context(spec: ContextSpec): Context = ContextBuilder().apply(spec).build()