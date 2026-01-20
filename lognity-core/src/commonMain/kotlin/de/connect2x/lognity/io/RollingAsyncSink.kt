package de.connect2x.lognity.io

import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.util.joinBlocking
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalAtomicApi::class)
class RollingAsyncSink( // @formatter:off
    val basePath: Path,
    val fileCount: Int,
    val maxFileSize: Long,
    val useTimestamps: Boolean
) : AutoCloseable { // @formatter:on
    private val fileNamePattern: Regex = compileFileNamePattern()
    private val channel: Channel<Sink.() -> Unit> = Channel(Channel.UNLIMITED)

    private val job: Job = DefaultBackend.coroutineScope.launch {
        var parentDir = basePath.parent
        if (parentDir == null) parentDir = SystemFileSystem.resolve(Path("."))
        else SystemFileSystem.createDirectories(parentDir)
        // @formatter:off
        SystemFileSystem.list(parentDir)
            .filter { path -> SystemFileSystem.metadataOrNull(path)?.isRegularFile == true && fileNamePattern.matches(path.name) }
            .forEach(SystemFileSystem::delete)
        // @formatter:on

        var fileIndex = 0
        var path = resolveFilePath(fileIndex)
        var sink = SystemFileSystem.sink(path).buffered()

        val pathBuffer = Array<Path?>(fileCount) { null }
        pathBuffer[0] = path

        try {
            for (task in channel) {
                sink.task()
                sink.flush()
                val fileSize = SystemFileSystem.metadataOrNull(path)?.size ?: 0L
                if (fileSize >= maxFileSize) {
                    fileIndex = (fileIndex + 1) % fileCount
                    sink.close()
                    path = resolveFilePath(fileIndex)
                    if (useTimestamps && pathBuffer.all { oldPath -> oldPath != null }) pathBuffer[fileIndex]?.let { oldPath ->
                        SystemFileSystem.delete(oldPath, mustExist = false)
                    }
                    pathBuffer[fileIndex] = path
                    SystemFileSystem.delete(path, mustExist = false)
                    sink = SystemFileSystem.sink(path).buffered()
                }
            }
        }
        finally {
            withContext(NonCancellable) {
                sink.close()
            }
        }
    }

    private fun compileFileNamePattern(): Regex {
        val timestampPattern = if (useTimestamps) "-(.+)" else ""
        val fileName = basePath.name
        return if ("." in fileName) {
            val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')).replace(".", "\\.")
            val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
            Regex("""$fileNameWithoutExt(\.[0-9]+)$timestampPattern(\.$fileExt)""")
        }
        else Regex("""$fileName(\.[0-9]+)$timestampPattern""")
    }

    @OptIn(ExperimentalTime::class)
    private fun resolveFilePath(index: Int): Path {
        val parentPath = basePath.parent ?: Path("")
        val fileName = basePath.name
        val timestamp =
            if (useTimestamps) "-${Clock.System.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)}"
            else ""
        if ('.' !in fileName) return Path(parentPath, "$fileName-$index$timestamp")
        val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'))
        val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
        return Path(parentPath, "$fileNameWithoutExt.$index$timestamp.$fileExt")
    }

    fun write(task: Sink.() -> Unit) {
        channel.trySend(task)
    }

    override fun close() {
        channel.close()
        job.joinBlocking()
    }
}