package de.connect2x.lognity.api.context

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Builder used to create a [Context] via a small DSL.
 */
@ContextDsl
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
     *
     * @param T The type of the element.
     * @param value The element to add.
     */
    fun <T : Context.Element> value(value: T) {
        values[value.key] = value
    }

    /**
     * DSL alias for [value].
     *
     * @param T The type of the element.
     * @param value The element to add.
     */
    operator fun <T : Context.Element> plusAssign(value: T) = value(value)

    @PublishedApi
    internal fun build(): Context = DefaultContext(values)
}

/**
 * Type alias for a Context builder specification used by [Context].
 */
typealias ContextSpec = ContextBuilder.() -> Unit

/**
 * Creates a new immutable [Context] using the provided [spec] DSL.
 *
 * Example:
 *
 * ```kotlin
 * val ctx = Context {
 *     this += UserId("42")
 * }
 * ```
 *
 * @param spec The DSL specification to build the context.
 * @return A new [Context] instance.
 */
@OptIn(ExperimentalContracts::class)
inline fun Context(spec: ContextSpec): Context {
    contract {
        callsInPlace(spec, InvocationKind.EXACTLY_ONCE)
    }
    return ContextBuilder().apply(spec).build()
}