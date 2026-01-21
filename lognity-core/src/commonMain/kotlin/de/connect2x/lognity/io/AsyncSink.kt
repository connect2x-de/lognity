package de.connect2x.lognity.io

import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.util.joinBlocking
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * An asynchronous, thread-safe wrapper around a [kotlinx.io.Sink] for file operations.
 *
 * It uses a [Channel] to queue write tasks which are executed sequentially by a
 * dedicated coroutine. This ensures that file I/O doesn't block the calling thread
 * and provides thread-safety for concurrent writes.
 *
 * @property path The file system path to write to.
 * @property deleteExisting If true, any existing file at [path] will be deleted before starting.
 */
class AsyncSink( // @formatter:off
    val path: Path,
    val deleteExisting: Boolean = false
) : AutoCloseable { // @formatter:on
    private val channel: Channel<Sink.() -> Unit> = Channel(Channel.UNLIMITED)

    private val job: Job = DefaultBackend.coroutineScope.launch {
        if (deleteExisting) SystemFileSystem.delete(path, mustExist = false)
        var sink = SystemFileSystem.sink(path, append = true).buffered()
        try {
            for (task in channel) sink.task()
        }
        finally {
            withContext(NonCancellable) {
                sink.close()
            }
        }
    }

    /**
     * Enqueues a write task to be executed asynchronously.
     *
     * @param task A lambda with [Sink] as receiver to perform write operations.
     */
    fun write(task: Sink.() -> Unit) {
        DefaultBackend.coroutineScope.launch {
            channel.send(task)
        }
    }

    /**
     * Closes the sink by closing the underlying channel and waiting for all pending tasks to complete.
     */
    override fun close() {
        channel.close()
        job.joinBlocking()
    }
}