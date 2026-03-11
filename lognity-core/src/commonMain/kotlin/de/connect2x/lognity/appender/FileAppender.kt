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
import kotlinx.io.files.Path
import kotlinx.io.writeString

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
) : Appender { // @formatter:on
    /**
     * The asynchronous sink used for writing to the file.
     */
    val sink: AsyncSink = AsyncSink(path, deleteExisting)

    init {
        ShutdownHandler.register(sink::close, priority = 99)
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        sink.write {
            writeString("${message.toAnsi().cleanString()}\n")
        }
    }
}