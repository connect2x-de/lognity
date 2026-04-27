package de.connect2x.lognity.io

import de.connect2x.lognity.api.backend.Backend
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.concurrent.atomics.AtomicLong
import kotlin.time.Clock

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
internal class RollingAsyncSink( // @formatter:off
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

    init {
        if (deleteExisting) deleteExisting()
    }

    private val initialState: Pair<Int, Path> = getInitialState()
    private var currentFileIndex: Int = initialState.first
    private var currentPath: Path = initialState.second
    private var sink: Sink = SystemFileSystem.sink(initialState.second, append = true).buffered()
    private val pathBuffer: Array<Path?> = restorePathBuffer(currentFileIndex, currentPath).apply {
        this[currentFileIndex] = currentPath
    }
    private val bytesWritten: AtomicLong = AtomicLong(0L)
    private val lock: Mutex = Mutex()
    private val channel: Channel<String> = Channel(Channel.UNLIMITED)

    private val job: Job = Backend.coroutineScope.launch {
        for (message in channel) {
            // Encode the string into UTF-8 ourselves, so we can easily get the encoded byte size
            val bytes = message.encodeToByteArray()
            lock.withLock {
                sink.write(bytes)
                rotateIfNeeded(bytes.size)
            }
        }
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

    private fun removeLatestSuffix(path: Path): Path {
        if (latestSuffix.isEmpty()) return path
        val name = path.name
        val result = latestFileNamePattern.matchEntire(name) ?: return path
        val rawName = result.groupValues.getOrNull(FILE_NAME_GROUP) ?: return path
        val fileIndex = result.groupValues.getOrNull(FILE_INDEX_GROUP) ?: return path

        val timestamp = if (useTimestamps) {
            val ts = result.groupValues.getOrNull(FILE_TIMESTAMP_GROUP) ?: ""
            "-$ts"
        }
        else ""

        val newFileName = if ("." in name) {
            val fileExt = name.substringAfterLast('.')
            "$rawName.$fileIndex$timestamp.$fileExt"
        }
        else {
            "$rawName.$fileIndex$timestamp"
        }
        return path.parent?.let { parent -> Path(parent, newFileName) } ?: Path(newFileName)
    }

    private fun compileFileNamePattern(matchLatestOnly: Boolean = false): Regex {
        val fileName = basePath.name
        val timestampPattern = if (useTimestamps) "-([0-9T\\-+.Z]+)" else ""
        val suffix = if (matchLatestOnly) {
            if (latestSuffix.isNotEmpty()) "(${latestSuffix.replace(".", "\\.")})" else ""
        }
        else {
            if (latestSuffix.isNotEmpty()) "(${latestSuffix.replace(".", "\\.")})?" else ""
        }

        return if ("." in fileName) {
            val fileNameWithoutExt = fileName.substringBeforeLast('.').replace(".", "\\.")
            val fileExt = fileName.substringAfterLast('.')
            Regex("""($fileNameWithoutExt)\.([0-9]+)$timestampPattern$suffix\.($fileExt)""")
        }
        else {
            Regex("""($fileName)\.([0-9]+)$timestampPattern$suffix""")
        }
    }

    private fun resolveLatestFilePath(index: Int): Path {
        val parentPath = basePath.parent ?: Path("")
        val fileName = basePath.name
        val timestamp = if (useTimestamps) "-${
            Clock.System.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET).replace(":", "")
        }"
        else ""
        if ('.' !in fileName) return Path(parentPath, "$fileName.$index$timestamp$latestSuffix")
        val fileNameWithoutExt = fileName.substringBeforeLast('.')
        val fileExt = fileName.substringAfterLast('.')
        return Path(parentPath, "$fileNameWithoutExt.$index$timestamp$latestSuffix.$fileExt")
    }

    private fun rotateIfNeeded(bytesWritten: Int) {
        if (this.bytesWritten.addAndFetch(bytesWritten.toLong()) == maxFileSize) {
            this.bytesWritten.store(0)
            sink.close()
            if (latestSuffix.isNotEmpty()) {
                val oldRenamedPath = removeLatestSuffix(currentPath)
                if (SystemFileSystem.exists(currentPath)) SystemFileSystem.atomicMove(currentPath, oldRenamedPath)
                pathBuffer[currentFileIndex] = oldRenamedPath
            }
            currentFileIndex = (currentFileIndex + 1) % fileCount
            currentPath = resolveLatestFilePath(currentFileIndex)
            if (pathBuffer[currentFileIndex] != null) {
                pathBuffer[currentFileIndex]?.let { oldPath ->
                    SystemFileSystem.delete(oldPath, mustExist = false)
                }
            }
            pathBuffer[currentFileIndex] = currentPath

            SystemFileSystem.delete(currentPath, mustExist = false)
            this.sink = SystemFileSystem.sink(currentPath).buffered()
        }
    }

    fun write(value: String) {
        channel.trySend(value)
    }

    suspend fun writeSuspend(value: String) {
        channel.send(value)
    }

    fun flush() = runBlocking {
        lock.withLock {
            sink.flush()
        }
    }

    suspend fun flushSuspend() = withContext(ioDispatcher) {
        lock.withLock {
            sink.flush()
        }
    }

    override fun close() {
        channel.close()
        runBlocking {
            job.join()
            lock.withLock {
                sink.close()
            }
        }
    }
}