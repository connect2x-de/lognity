package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.ShutdownHandler
import de.connect2x.lognity.io.AsyncSink
import kotlinx.io.files.Path

/**
 * An appender that writes log messages to a file.
 *
 * @property pattern The pattern used for formatting log messages.
 * @property formatter The formatter used to format log events.
 * @property filter The filter used to decide whether to log a message.
 * @property path The path to the log file.
 * @property name The optional name of the appender.
 * @param deleteExisting If true, any existing file at the given path will be deleted upon initialization.
 */
class FileAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    val path: Path,
    override val name: String? = null,
    deleteExisting: Boolean = false
) : AbstractAppender() { // @formatter:on
    private val sink: AsyncSink = AsyncSink(path, deleteExisting)

    init {
        ShutdownHandler.register(sink::close, priority = 99)
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        sink.write("${message.toAnsi().cleanString()}\n")
    }

    @ExperimentalLoggingApi
    override suspend fun appendSuspend(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        sink.writeSuspend("${message.toAnsi().cleanString()}\n")
    }

    override fun flush() = sink.flush()

    @ExperimentalLoggingApi
    override suspend fun flushSuspend() = sink.flushSuspend()
}