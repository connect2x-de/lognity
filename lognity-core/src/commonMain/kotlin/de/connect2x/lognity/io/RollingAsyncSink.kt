package de.connect2x.lognity.io

import de.connect2x.lognity.backend.DefaultBackend
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.io.InternalIoApi
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class RollingAsyncSink( // @formatter:off
    val basePath: Path,
    val fileCount: Int,
    val maxFileSize: Long
) : AutoCloseable { // @formatter:on
    companion object {
        private fun suffixFileName(path: Path, index: Int): Path {
            val parentPath = path.parent ?: Path("")
            val fileName = path.name
            if ('.' !in fileName) return Path(parentPath, "$fileName-$index")
            val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'))
            val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
            return Path(parentPath, "$fileNameWithoutExt.$index.$fileExt")
        }
    }

    private val channel: Channel<Sink.() -> Unit> = Channel(Channel.UNLIMITED)

    @OptIn(InternalIoApi::class)
    private val job: Job = DefaultBackend.coroutineScope.launch {
        for (i in 0..<fileCount) { // Delete any residual log files
            SystemFileSystem.delete(suffixFileName(basePath, i), mustExist = false)
        }
        var fileIndex = 0
        var path = suffixFileName(basePath, fileIndex)
        var sink = SystemFileSystem.sink(path).buffered()
        try {
            for (task in channel) {
                sink.task()
                sink.emit() // Ensure new data is emitted, not flushed
                val fileSize = SystemFileSystem.metadataOrNull(path)?.size ?: 0L
                if (fileSize >= maxFileSize) {
                    if (fileIndex < fileCount) fileIndex++
                    else fileIndex = 0
                    sink.close()
                    path = suffixFileName(basePath, fileIndex)
                    SystemFileSystem.delete(path, mustExist = false)
                    sink = SystemFileSystem.sink(path).buffered()
                }
            }
        }
        finally {
            sink.close()
        }
    }

    fun write(task: Sink.() -> Unit) {
        channel.trySend(task)
    }

    override fun close() {
        channel.close()
        job.cancel()
    }
}