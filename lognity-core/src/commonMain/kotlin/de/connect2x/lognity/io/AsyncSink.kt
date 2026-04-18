package de.connect2x.lognity.io

import de.connect2x.lognity.util.Mutex
import de.connect2x.lognity.util.withLock
import de.connect2x.lognity.util.withLockSuspend
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

internal class AsyncSink( // @formatter:off
    val path: Path,
    val deleteExisting: Boolean = false
) : AutoCloseable { // @formatter:on
    init {
        if (deleteExisting) SystemFileSystem.delete(path, mustExist = false)
    }

    private val sink: Sink = SystemFileSystem.sink(path, append = true).buffered()
    private val lock: Mutex = Mutex()

    fun write(value: String) = lock.withLock {
        sink.writeString(value)
    }

    suspend fun writeSuspend(value: String) = withContext(ioDispatcher) {
        lock.withLockSuspend {
            sink.writeString(value)
        }
    }

    fun flush() = lock.withLock {
        sink.flush()
    }

    suspend fun flushSuspend() = withContext(ioDispatcher) {
        lock.withLockSuspend {
            sink.flush()
        }
    }

    override fun close() = lock.withLock {
        sink.close()
    }
}