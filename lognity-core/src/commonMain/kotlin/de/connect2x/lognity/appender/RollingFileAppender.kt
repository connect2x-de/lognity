package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.io.MessageAggregator
import de.connect2x.lognity.io.SynchronizedSink
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class RollingFileAppender(
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    val basePath: Path,
    override val name: String? = null,
    val fileCount: Int = 10,
    val maxFileSize: Long = 1024 * 50 // 50kB per default
) : AbstractAggregatingAppender() {
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
    private var sink: SynchronizedSink? = null

    override suspend fun writeToOutput(message: MessageAggregator.Message) {
        if (sink == null) {
            sink = DefaultBackend.sinkCache.getOrOpenSink(getCurrentFilePath())
        }
        sink?.synchronized {
            writeString("${message.message.toAnsi().cleanString()}\n")
        }
        rotateFilesIfNeeded()
    }

    override suspend fun afterAggregatorShutdown() {
        sink?.close()
    }

    private fun getCurrentFilePath(): Path = suffixFileName(basePath, currentIndex.load())

    private fun getCurrentFileSize(): Long = SystemFileSystem.metadataOrNull(getCurrentFilePath())?.size ?: 0L

    private fun rotateFiles() {
        var currentIndex = currentIndex.load()
        if (currentIndex < fileCount) {
            currentIndex++
        }
        else {
            currentIndex = 0
        }
        this.currentIndex.store(currentIndex)
        sink?.close()
        sink = DefaultBackend.sinkCache.getOrOpenSink(getCurrentFilePath())
    }

    private fun rotateFilesIfNeeded() {
        if (getCurrentFileSize() < maxFileSize) return
        rotateFiles()
    }
}