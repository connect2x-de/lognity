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
    fileCount: Int = 10,
    maxFileSize: Long = 1024 * 50, // 50kB per default
    useTimestamps: Boolean = true
) : Appender {
    private val sink: RollingAsyncSink = RollingAsyncSink(basePath, fileCount, maxFileSize, useTimestamps)

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