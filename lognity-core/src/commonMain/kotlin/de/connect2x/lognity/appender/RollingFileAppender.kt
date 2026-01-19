package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.ShutdownHandler
import de.connect2x.lognity.io.AsyncSink
import de.connect2x.lognity.util.RefCounted
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
class RollingFileAppender(
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    val basePath: Path,
    override val name: String? = null,
    val fileCount: Int = 10,
    val maxFileSize: Long = 1024 * 50 // 50kB per default
) : Appender {
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

    private var currentIndex: AtomicInt = AtomicInt(0)
    private var sink: RefCounted<AsyncSink> = AsyncSink.getOrOpen(getCurrentFilePath())
    private val rotationMutex: Mutex = Mutex()

    init {
        ShutdownHandler.register({ sink.release() }, priority = 99)
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || message.isEmpty() || !filter(level, message, marker)) return
        sink.value.write {
            writeString("${message.toAnsi().cleanString()}\n")
            rotateFileIfNeeded()
        }
    }

    private fun getCurrentFilePath(): Path = suffixFileName(basePath, currentIndex.load())

    private fun getCurrentFileSize(): Long = SystemFileSystem.metadataOrNull(getCurrentFilePath())?.size ?: 0L

    private suspend fun rotateFileIfNeeded() = rotationMutex.withLock {
        if (getCurrentFileSize() < maxFileSize) return@withLock
        rotateFile()
    }

    private fun rotateFile() {
        if (currentIndex.incrementAndFetch() == fileCount - 1) {
            currentIndex.store(0)
        }
        sink.release()
        val path = getCurrentFilePath()
        SystemFileSystem.delete(path, mustExist = false)
        sink = AsyncSink.getOrOpen(path)
    }
}