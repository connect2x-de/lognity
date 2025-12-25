package de.connect2x.lognity.util

import kotlinx.io.Sink
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
internal data class RefCountedSink(
    val value: Sink
) {
    private var refCount: AtomicInt = AtomicInt(0)

    @Suppress("NOTHING_TO_INLINE")
    inline fun acquire(): RefCountedSink {
        refCount.incrementAndFetch()
        return this
    }

    inline fun release(releaseAction: () -> Unit = {}): RefCountedSink {
        if (refCount.load() == 0) return this
        if (refCount.decrementAndFetch() == 0) {
            releaseAction()
            value.close()
            return this
        }
        return this
    }
}