/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.util

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