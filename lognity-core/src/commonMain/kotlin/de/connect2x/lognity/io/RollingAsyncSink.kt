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

/**
 * An asynchronous, thread-safe sink that supports rolling files based on size and count.
 *
 * It uses a [Channel] to queue write tasks which are executed sequentially by a
 * dedicated coroutine. This ensures that file I/O doesn't block the calling thread
 * and provides thread-safety for concurrent writes.
 *
 * @property basePath The base file system path for the log files.
 * @property fileCount The maximum number of log files to keep.
 * @property maxFileSize The maximum size in bytes for a single log file before rolling.
 * @property useTimestamps Whether to include timestamps in the rolled file names.
 * @property deleteExisting Whether to delete all existing log files matching the pattern on startup.
 * @property latestSuffix The suffix used for the current active log file.
 */
@OptIn(ExperimentalAtomicApi::class)
class RollingAsyncSink( // @formatter:off
    val basePath: Path,
    val fileCount: Int,
    val maxFileSize: Long,
    val useTimestamps: Boolean,
    val deleteExisting: Boolean,
    val latestSuffix: String
) : AutoCloseable { // @formatter:on
    companion object {
        /**
         * Group index for the base file name in the regex match.
         */
        const val FILE_NAME_GROUP: Int = 1

        /**
         * Group index for the file sequence number in the regex match.
         */
        const val FILE_INDEX_GROUP: Int = 2

        /**
         * Group index for the timestamp in the regex match.
         */
        const val FILE_TIMESTAMP_GROUP: Int = 3
    }

    private val fileNamePattern: Regex = compileFileNamePattern()
    private val latestFileNamePattern: Regex = compileFileNamePattern(true)
    private val channel: Channel<Sink.() -> Unit> = Channel(Channel.UNLIMITED)

    private val parentDir: Path = run {
        var parentDir = basePath.parent
        if (parentDir == null) parentDir = SystemFileSystem.resolve(Path("."))
        else SystemFileSystem.createDirectories(parentDir)
        parentDir
    }

    private fun Path.isRegularFile(): Boolean = SystemFileSystem.metadataOrNull(this)?.isRegularFile == true

    private fun deleteExisting() {
        // @formatter:off
        SystemFileSystem.list(parentDir)
            .filter { path -> path.isRegularFile() && fileNamePattern.matches(path.name) }
            .forEach(SystemFileSystem::delete)
        // @formatter:on
    }

    private fun getInitialState(): Pair<Int, Path> {
        // If we delete all existing on restart, we can just derive a new path for file 0
        var fileIndex = 0
        val path = if (deleteExisting) resolveLatestFilePath(fileIndex)
        else {
            var latestFilePath = SystemFileSystem.list(parentDir)
                .find { path -> path.isRegularFile() && latestFileNamePattern.matches(path.name) }
            if (latestFilePath == null) { // If we can't figure out the latest file, start fresh
                deleteExisting()
                latestFilePath = resolveLatestFilePath(fileIndex)
            }
            else {
                // If we found the latest file, we parse the current segment number to pick up where we left off
                val result = latestFileNamePattern.matchEntire(latestFilePath.name)
                fileIndex =
                    (result?.groupValues?.getOrNull(FILE_INDEX_GROUP)?.toIntOrNull() ?: 0).coerceIn(0..<fileCount)
            }
            latestFilePath
        }
        return fileIndex to path
    }

    private fun restorePathBuffer(fileIndex: Int, latestPath: Path): Array<Path?> {
        val pathBuffer = Array<Path?>(fileCount) { null }
        pathBuffer[fileIndex] = latestPath
        if (!deleteExisting) {
            // Load all inactive segment paths into the buffer when present
            SystemFileSystem.list(parentDir)
                .filter { path -> path.isRegularFile() && fileNamePattern.matches(path.name) }
                .forEach { path ->
                    val result = fileNamePattern.matchEntire(path.name) ?: return@forEach
                    val inactiveFileIndex =
                        result.groupValues.getOrNull(FILE_INDEX_GROUP)?.toIntOrNull() ?: return@forEach
                    if (inactiveFileIndex == fileIndex || inactiveFileIndex >= fileCount) return@forEach // Skip latest
                    pathBuffer[inactiveFileIndex] = path
                }
        }
        return pathBuffer
    }

    private val job: Job = DefaultBackend.coroutineScope.launch {
        if (deleteExisting) deleteExisting()

        var (fileIndex, path) = getInitialState()
        val initialSize = SystemFileSystem.metadataOrNull(path)?.size ?: 0L
        var sink = SystemFileSystem.sink(path, append = true).asCounting(initialSize)
        var bufferedSink = sink.buffered()
        val pathBuffer = restorePathBuffer(fileIndex, path)

        try {
            for (task in channel) {
                bufferedSink.task()
                if (sink.bytesWritten >= maxFileSize) {
                    bufferedSink.close()

                    if (latestSuffix.isNotEmpty()) {
                        val oldRenamedPath = removeLatestSuffix(path)
                        SystemFileSystem.atomicMove(path, oldRenamedPath)
                        pathBuffer[fileIndex] = oldRenamedPath
                    }

                    fileIndex = (fileIndex + 1) % fileCount
                    path = resolveLatestFilePath(fileIndex)
                    if (pathBuffer.all { oldPath -> oldPath != null }) pathBuffer[fileIndex]?.let { oldPath ->
                        SystemFileSystem.delete(oldPath, mustExist = false)
                    }
                    pathBuffer[fileIndex] = path

                    SystemFileSystem.delete(path, mustExist = false)
                    sink = SystemFileSystem.sink(path).asCounting()
                    bufferedSink = sink.buffered()
                }
            }
        }
        finally {
            withContext(NonCancellable) {
                bufferedSink.close()
            }
        }
    }

    private fun removeLatestSuffix(path: Path): Path {
        val name = path.name
        val result = latestFileNamePattern.matchEntire(name) ?: return path
        val rawName = result.groupValues.getOrNull(FILE_NAME_GROUP) ?: return path
        val fileIndex = result.groupValues.getOrNull(FILE_INDEX_GROUP) ?: return path

        if (useTimestamps) {
            val timestamp = result.groupValues.getOrNull(FILE_TIMESTAMP_GROUP) ?: return path
            val newFileName = if ("." in name) {
                val fileExt = name.substring(name.lastIndexOf('.') + 1)
                "$rawName.$fileIndex-$timestamp.$fileExt"
            }
            else "$rawName.$fileIndex-$timestamp"
            return path.parent?.let { parent -> Path(parent, newFileName) } ?: Path(newFileName)
        }

        val newFileName = if ("." in name) {
            val fileExt = name.substring(name.lastIndexOf('.') + 1)
            "$rawName.$fileIndex.$fileExt"
        }
        else "$rawName.$fileIndex"
        return path.parent?.let { parent -> Path(parent, newFileName) } ?: Path(newFileName)
    }

    private fun compileFileNamePattern(matchLatestOnly: Boolean = false): Regex {
        val timestampPattern = if (useTimestamps) "\\-(.+)" else ""
        val fileName = basePath.name
        val suffix =
            if (matchLatestOnly) "(${latestSuffix.replace(".", "\\.")})" else "(${latestSuffix.replace(".", "\\.")})?"
        return if ("." in fileName) {
            val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')).replace(".", "\\.")
            val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
            Regex("""($fileNameWithoutExt)\.([0-9]+)$timestampPattern$suffix\.($fileExt)""")
        }
        else Regex("""($fileName)\.([0-9]+)$timestampPattern$suffix""")
    }

    @OptIn(ExperimentalTime::class)
    private fun resolveLatestFilePath(index: Int): Path {
        val parentPath = basePath.parent ?: Path("")
        val fileName = basePath.name
        val timestamp =
            if (useTimestamps) "-${Clock.System.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)}"
            else ""
        if ('.' !in fileName) return Path(parentPath, "$fileName-$index$timestamp$latestSuffix")
        val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'))
        val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
        return Path(parentPath, "$fileNameWithoutExt.$index$timestamp$latestSuffix.$fileExt")
    }

    /**
     * Enqueues a write task to be executed asynchronously.
     *
     * @param task A lambda with [Sink] as receiver to perform write operations.
     */
    fun write(task: Sink.() -> Unit) {
        channel.trySend(task)
    }

    /**
     * Closes the sink by closing the underlying channel and waiting for all pending tasks to complete.
     */
    override fun close() {
        channel.close()
        job.joinBlocking()
    }
}