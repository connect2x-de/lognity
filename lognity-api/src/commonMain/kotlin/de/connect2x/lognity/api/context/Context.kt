package de.connect2x.lognity.api.context

import de.connect2x.lognity.api.context.Context.Key

/**
 * A lightweight container that carries structured context information for log events.
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
    interface Key<T : Element>

    /**
     * Returns all elements associated with this context.
     */
    val elements: Map<Key<*>, Element>

    /**
     * Returns the element associated with [key] or null if none exist.
     */
    operator fun <T : Element> get(key: Key<T>): T?

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
    override val elements: Map<Key<*>, Context.Element> = emptyMap()
    override fun <T : Context.Element> get(key: Key<T>): T? = null
    override fun plus(other: Context): Context = other
}

internal data class DefaultContext(
    override val elements: Map<Key<*>, Context.Element>
) : Context {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Context.Element> get(key: Key<T>): T? {
        return elements[key] as? T
    }

    override fun plus(other: Context): Context = DefaultContext(elements + other.elements)
}