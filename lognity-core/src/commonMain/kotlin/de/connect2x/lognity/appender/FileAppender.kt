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
import kotlinx.io.writeString

/**
 * Appender that writes log messages to a file.
 *
 * This implementation
 * - reuses a shared buffered Sink per file path across instances using simple reference counting,
 * - is thread-safe via a Mutex around writes,
 * - strips ANSI escape sequences from messages before writing to keep the file clean, and
 * - registers a shutdown hook with the Backend to flush and close the file handle when the process ends.
 *
 * @property pattern The formatting pattern string used by this appender. Passed to and interpreted by [formatter].
 * @property formatter The formatter that produced the final message from [pattern] and log context.
 * @property filter A filter that decides whether a given message should be written.
 * @property path The file system path to which log lines will be appended. The file is opened in append mode.
 */
class FileAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    val path: Path,
    override val name: String? = null,
) : Appender { // @formatter:on
    val sink: RefCounted<AsyncSink> = AsyncSink.getOrOpen(path)

    init {
        ShutdownHandler.register(sink::release, priority = 99)
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || message.isEmpty() || !filter(level, message, marker)) return
        sink.value.write {
            writeString("${message.toAnsi().cleanString()}\n")
        }
    }
}