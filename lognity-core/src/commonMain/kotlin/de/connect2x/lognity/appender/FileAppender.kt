package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.util.RefCountedSink
import kotlinx.coroutines.sync.withLock
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
open class FileAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    val path: Path,
    override val name: String? = null
) : AbstractAggregatingAppender() { // @formatter:on
    companion object {
        internal val sinks: SharedHashMap<Path, RefCountedSink> = SharedHashMap()
    }

    internal val sink: RefCountedSink = sinks.getOrPut(path) {
        RefCountedSink(SystemFileSystem.sink(path, true).buffered())
    }.acquire()

    override suspend fun writeToOutput(message: MessageAggregator.Message) {
        sink.mutex.withLock {
            sink.value.writeString("${message.message.toAnsi().cleanString()}\n")
        }
    }

    override fun afterAggregatorShutdown() {
        flush()
        sink.release { sinks -= path }
    }

    override fun flush() = sink.value.flush()
}