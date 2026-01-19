package de.connect2x.lognity.io

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.util.RefCounted
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

class AsyncSink(
    val path: Path
) : AutoCloseable {
    companion object {
        private val sinks: SharedHashMap<Path, RefCounted<AsyncSink>> = SharedHashMap()

        fun getOrOpen(path: Path): RefCounted<AsyncSink> = sinks.getOrPut(path) {
            RefCounted(AsyncSink(path))
        }.apply {
            acquire()
        }
    }

    private val channel: Channel<Sink.() -> Unit> = Channel(Channel.BUFFERED)

    private val job: Job = DefaultBackend.coroutineScope.launch {
        val sink = SystemFileSystem.sink(path).buffered()
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
        channel.trySend(task)
    }

    override fun close() {
        channel.close()
        job.joinBlocking()
    }
}