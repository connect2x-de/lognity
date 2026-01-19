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
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
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

    private val currentIndex: AtomicInt = AtomicInt(0)
    private val sink: AtomicReference<RefCounted<AsyncSink>> =
        AtomicReference(AsyncSink.getOrOpen(getCurrentFilePath()))

    init {
        ShutdownHandler.register({ sink.load().release() }, priority = 99)
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || message.isEmpty() || !filter(level, message, marker)) return
        sink.load().value.write {
            writeString("${message.toAnsi().cleanString()}\n")
        }
        rotateFilesIfNeeded()
    }

    private fun getCurrentFilePath(): Path = suffixFileName(basePath, currentIndex.load())

    private fun getCurrentFileSize(): Long = SystemFileSystem.metadataOrNull(getCurrentFilePath())?.size ?: 0L

    private fun rotateFiles() {
        if (currentIndex.incrementAndFetch() == fileCount) {
            currentIndex.store(0)
        }
        sink.exchange(AsyncSink.getOrOpen(getCurrentFilePath())).release()
    }

    private fun rotateFilesIfNeeded() {
        if (getCurrentFileSize() < maxFileSize) return
        rotateFiles()
    }
}