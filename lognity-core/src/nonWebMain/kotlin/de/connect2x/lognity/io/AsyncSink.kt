package de.connect2x.lognity.io

import de.connect2x.lognity.api.backend.Backend
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

internal class AsyncSink( // @formatter:off
    path: Path,
    deleteExisting: Boolean = false
) { // @formatter:on
    init {
        if (deleteExisting) SystemFileSystem.delete(path, mustExist = false)
    }

    private val sink: Sink = SystemFileSystem.sink(path, append = true).buffered()
    private val lock: Mutex = Mutex()
    private val channel: Channel<String> = Channel(Channel.UNLIMITED)

    private val job: Job = Backend.coroutineScope.launch {
        for (message in channel) {
            lock.withLock {
                sink.writeString(message)
            }
        }
    }

    fun write(value: String) {
        channel.trySend(value)
    }

    suspend fun writeSuspend(value: String) {
        channel.send(value)
    }

    suspend fun flushSuspend() = lock.withLock {
        sink.flush()
    }

    fun flush() = runBlocking {
        flushSuspend()
    }

    fun close() {
        channel.close()
        runBlocking {
            job.join()
            lock.withLock {
                sink.close()
            }
        }
    }
}