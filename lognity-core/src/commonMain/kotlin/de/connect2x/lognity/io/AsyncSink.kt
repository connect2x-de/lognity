package de.connect2x.lognity.io

import de.connect2x.lognity.backend.DefaultBackend
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class AsyncSink(
    val path: Path
) : AutoCloseable {
    private val channel: Channel<suspend Sink.() -> Unit> = Channel(Channel.UNLIMITED)

    private val job: Job = DefaultBackend.coroutineScope.launch {
        var sink = SystemFileSystem.sink(path).buffered()
        try {
            for (task in channel) sink.task()
        }
        finally {
            channel.consumeEach { task -> sink.task() }
            sink.close()
        }
    }

    fun write(task: suspend Sink.() -> Unit) {
        channel.trySend(task)
    }

    override fun close() {
        channel.close()
        job.cancel()
    }
}