package de.connect2x.lognity.io

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.util.RefCounted
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class SinkCache {
    private val sinks: SharedHashMap<Path, SynchronizedSink> = SharedHashMap()

    fun getOrOpenSink(path: Path): SynchronizedSink {
        return sinks.getOrPut(path) {
            SynchronizedSink(RefCounted( // @formatter:off
                value = SystemFileSystem.sink(path).buffered(),
                defaultReleaseAction = { sink ->
                    sink.close()
                    sinks -= path
                }
            )) // @formatter:on
        }.apply {
            sink.acquire() // Increment internal refcount
        }
    }
}