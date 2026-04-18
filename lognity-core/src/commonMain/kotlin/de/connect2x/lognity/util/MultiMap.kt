package de.connect2x.lognity.util

internal class MultiMap<K, V>(
    private val delegate: HashMap<K, MutableList<V>> = HashMap()
) : MutableMap<K, MutableList<V>> by delegate {
    fun putSingle(key: K, value: V) {
        delegate.getOrPut(key) { ArrayList() } += value
    }

    operator fun set(key: K, value: V) = putSingle(key, value)
}