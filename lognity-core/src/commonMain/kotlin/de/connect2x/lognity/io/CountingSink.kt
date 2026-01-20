package de.connect2x.lognity.io

import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class CountingSink( // @formatter:off
    private val delegate: RawSink,
    initialBytesWritten: Long
) : RawSink { // @formatter:on
    override fun close() = delegate.close()
    override fun flush() = delegate.flush()

    @PublishedApi
    internal val _bytesWritten: AtomicLong = AtomicLong(initialBytesWritten)
    inline val bytesWritten: Long get() = _bytesWritten.load()

    override fun write(source: Buffer, byteCount: Long) {
        delegate.write(source, byteCount)
        _bytesWritten.addAndFetch(byteCount)
    }
}

internal fun RawSink.asCounting(bytesWritten: Long = 0L): CountingSink = CountingSink(this, bytesWritten)