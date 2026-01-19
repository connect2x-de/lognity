package de.connect2x.lognity.util

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalAtomicApi::class)
class RefCounted<T : AutoCloseable>( // @formatter:off
    initialValue: T,
    val defaultReleaseAction: (T) -> Unit = {}
) { // @formatter:on
    @PublishedApi
    internal val _value: AtomicReference<T> = AtomicReference(initialValue)

    inline var value: T
        get() = _value.load()
        set(value) {
            _value.store(value)
        }

    @PublishedApi
    internal val count: AtomicInt = AtomicInt(0)

    fun acquire(): RefCounted<T> {
        count.incrementAndFetch()
        return this
    }

    @OptIn(ExperimentalContracts::class)
    fun release(): RefCounted<T> {
        if (count.decrementAndFetch() > 0) return this
        defaultReleaseAction(value)
        value.close()
        return this
    }
}