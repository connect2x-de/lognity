package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
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
) : AbstractAggregatingAppender() { // @formatter:on
    val sink: Sink = SystemFileSystem.sink(path).buffered()

    override suspend fun writeToOutput(message: MessageAggregator.Message) {
        sink.writeString("${message.message.toAnsi().cleanString()}\n")
    }

    override suspend fun afterAggregatorShutdown() = sink.close()

    override fun flush() = sink.flush()
}