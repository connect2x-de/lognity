package de.connect2x.lognity.io

import de.connect2x.lognity.util.RefCounted
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Sink

data class SynchronizedSink( // @formatter:off
    @PublishedApi internal val sink: RefCounted<Sink>,
    @PublishedApi internal val mutex: Mutex = Mutex()
) : AutoCloseable { // @formatter:on
    suspend inline fun <R> synchronized(crossinline block: Sink.() -> R): R = mutex.withLock {
        sink.value.block()
    }

    fun flush() = sink.value.flush()

    override fun close() {
        sink.release()
    }
}