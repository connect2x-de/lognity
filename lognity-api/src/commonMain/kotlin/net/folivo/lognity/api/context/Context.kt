package net.folivo.lognity.api.context

import net.folivo.lognity.api.context.Context.Key


/**
 * A lightweight container that carries structured context information for log events.
 *
 * Context is immutable. Operations like [plus] and [fold] create derived values without
 * mutating the original. A [Context] can hold multiple [Element]s for the same [Key];
 * use [fold] to traverse all of them. The order of traversal is not guaranteed and
 * should not be relied upon.
 *
 * This concept is inspired by Kotlin's CoroutineContext but adapted to logging needs:
 * - Multiple elements per key are allowed (e.g., multiple tags).
 * - [get] returns the first element for the key if present; prefer [fold] if you care
 *   about all values.
 */
interface Context {
    /**
     * A single piece of context information.
     *
     * Each element must expose the [key] that identifies the group it belongs to.
     */
    interface Element {
        /** The key that identifies this element's group within the context. */
        val key: Key<*>
    }

    /**
     * A type-safe key used to group and retrieve [Element]s of the same conceptual kind.
     *
     * A typical implementation is an `object` declaration per key, for example:
     *
     * object UserIdKey : Context.Key<UserId?>
     */
    interface Key<T : Element?>

    /**
     * Accumulates a value across all [Element]s contained in this context.
     *
     * Note: The iteration order is unspecified. Do not depend on it.
     */
    fun <R> fold(initial: R, transform: (R, Element) -> R): R

    /**
     * Accumulates a value across all elements associated with the given [key].
     *
     * Elements are provided to [transform] in an unspecified order.
     */
    fun <T : Element?, R> fold(key: Key<T>, initial: R, transform: (R, T) -> R): R

    /**
     * Returns the first element associated with [key] or null if none exist.
     *
     * If you expect multiple values, prefer [fold] to process them all.
     */
    operator fun <T : Element?> get(key: Key<T>): T?

    /**
     * Creates a new context that contains the elements of this context plus those
     * from [other]. If the same [Key] appears in both, elements are merged; duplicates
     * (by equality) are de-duplicated.
     */
    operator fun plus(other: Context): Context
}

/**
 * A [Context] with no elements.
 */
object EmptyContext : Context {
    override fun <R> fold(initial: R, transform: (R, Context.Element) -> R): R = initial

    override fun <T : Context.Element?, R> fold(
        key: Key<T>, initial: R, transform: (R, T) -> R
    ): R = initial

    override fun <T : Context.Element?> get(key: Key<T>): T? = null
    override fun plus(other: Context): Context = other
}

/**
 * Default immutable [Context] implementation used internally.
 *
 * It stores elements in sets per [Key] to avoid duplicates while allowing multiple
 * elements per key. The iteration order is not guaranteed.
 */
internal data class DefaultContext(
    private val values: Map<Key<*>, Set<Context.Element>>
) : Context {
    override fun <R> fold(initial: R, transform: (R, Context.Element) -> R): R {
        var value = initial
        for ((_, elements) in values) {
            val elementsList = elements.toList()
            for (element in elementsList) {
                value = transform(value, element)
            }
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Context.Element?, R> fold(
        key: Key<T>, initial: R, transform: (R, T) -> R
    ): R {
        val elements = values[key]?.toList() ?: return initial
        var value = initial
        for (element in elements) {
            value = transform(value, element as? T ?: continue)
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Context.Element?> get(key: Key<T>): T? {
        return values[key]?.firstOrNull() as? T
    }

    override fun plus(other: Context): Context = DefaultContext( // @formatter:off
        other.fold(values.mapValues { (_, elements) ->
            elements.toMutableSet()
        }.toMutableMap()) { map, element ->
            map.getOrPut(element.key) { HashSet() } += element
            map
        }
    ) // @formatter:on
}