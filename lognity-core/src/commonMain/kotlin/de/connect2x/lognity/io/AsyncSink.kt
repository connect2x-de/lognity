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

    fun write(task: Sink.() -> Unit) {
        DefaultBackend.coroutineScope.launch {
            channel.send(task)
        }
    }

    override fun close() {
        channel.close()
        job.joinBlocking()
    }
}