package de.connect2x.lognity.util

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalAtomicApi::class)
class RefCounted<T>( // @formatter:off
    val value: T,
    val defaultReleaseAction: (T) -> Unit = {}
) { // @formatter:on
    @PublishedApi
    internal val count: AtomicInt = AtomicInt(0)

    fun acquire(): RefCounted<T> {
        count.incrementAndFetch()
        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun release(block: (T) -> Unit = {}): RefCounted<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (count.decrementAndFetch() > 0) return this
        defaultReleaseAction(value)
        block(value)
        return this
    }
}