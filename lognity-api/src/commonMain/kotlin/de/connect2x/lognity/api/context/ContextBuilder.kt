package de.connect2x.lognity.api.context

/**
 * Builder used to create a [Context] via a small DSL.
 */
class ContextBuilder @PublishedApi internal constructor() {
    private val values: HashMap<Context.Key<*>, Context.Element> = HashMap()

    /**
     * Copies all elements from the given [context] into this builder.
     *
     * Elements are merged by their [Context.Key].
     */
    fun valuesFrom(context: Context) {
        values += context.elements
    }

    /**
     * Adds multiple elements grouped by key.
     */
    fun values(values: Map<Context.Key<*>, Context.Element>) {
        this.values += values
    }

    /**
     * DSL alias for [values].
     */
    operator fun plusAssign(values: Map<Context.Key<*>, Context.Element>) = values(values)

    /**
     * Adds all given elements by their associated key.
     */
    fun values(values: Iterable<Context.Element>) {
        for (value in values) {
            this += value
        }
    }

    /**
     * DSL alias for [values].
     */
    operator fun plusAssign(values: Iterable<Context.Element>) = values(values)

    /**
     * Adds the given [value].
     */
    fun <T : Context.Element> value(value: T) {
        values[value.key] = value
    }

    /**
     * DSL alias for [value].
     */
    operator fun <T : Context.Element> plusAssign(value: T) = value(value)

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