package de.connect2x.lognity.util

internal class MultiMap<K, V>(
    private val delegate: HashMap<K, MutableList<V>> = HashMap()
) : MutableMap<K, MutableList<V>> by delegate {
    constructor(initialSize: Int) : this(HashMap(initialSize))

    constructor(vararg pairs: Pair<K, V>) : this(HashMap<K, MutableList<V>>().apply {
        for ((key, value) in pairs) {
            getOrPut(key) { ArrayList() } += value
        }
    })

    fun putSingle(key: K, value: V) {
        delegate.getOrPut(key) { ArrayList() } += value
    }

    inline fun findSingle(key: K, predicate: (V) -> Boolean): V? {
        return delegate[key]?.find(predicate)
    }

    operator fun set(key: K, value: V) = putSingle(key, value)

    fun removeSingle(key: K, value: V) {
        delegate[key]?.let { list ->
            list -= value
            if (list.isEmpty()) delegate -= key
        }
    }
}