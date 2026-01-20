package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.ShutdownHandler
import de.connect2x.lognity.io.RollingAsyncSink
import kotlinx.io.files.Path
import kotlinx.io.writeString
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class RollingFileAppender(
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    basePath: Path,
    override val name: String? = null,
    fileCount: Int = DEFAULT_FILE_COUNT,
    maxFileSize: Long = DEFAULT_FILE_SIZE,
    useTimestamps: Boolean = true,
    deleteExisting: Boolean = false,
    latestSuffix: String = DEFAULT_LATEST_SUFFIX
) : Appender {
    companion object {
        const val DEFAULT_FILE_COUNT: Int = 8
        const val DEFAULT_FILE_SIZE: Long = 1024 * 1024 * 10 // 10MB
        const val DEFAULT_LATEST_SUFFIX: String = "-latest"
    }

    private val sink: RollingAsyncSink =
        RollingAsyncSink(basePath, fileCount, maxFileSize, useTimestamps, deleteExisting, latestSuffix)

    init {
        ShutdownHandler.register(sink::close, priority = 99)
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || message.isEmpty() || !filter(level, message, marker)) return
        sink.write {
            writeString("${message.toAnsi().cleanString()}\n")
        }
    }
}