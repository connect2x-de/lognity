package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.withBlockingLock
import de.connect2x.lognity.util.RefCountedSink
import kotlinx.coroutines.sync.Mutex
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
) : Appender { // @formatter:on
    companion object {
        internal val sinks: SharedHashMap<Path, RefCountedSink> = SharedHashMap()
    }

    init {
        Backend.addShutdownHook(::dispose)
    }

    internal val sink: RefCountedSink = sinks.getOrPut(path) {
        RefCountedSink(SystemFileSystem.sink(path, true).buffered())
    }.acquire()

    private val mutex: Mutex = Mutex()

    /**
     * Appends the given message to the configured [path] as a single line.
     *
     * Behavior:
     * - Respects [filter]; returns immediately if it rejects the message.
     * - Removes ANSI escape sequences from [message] to ensure clean file output.
     * - Performs the write under a mutex to guarantee thread-safety across concurrent loggers.
     *
     * @param logger The logger that produced the message.
     * @param level The log level of the message.
     * @param message The formatted log message to write.
     * @param marker Optional marker associated with the message.
     */
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        // Make sure to strip out any ANSI codes when writing to file
        mutex.withBlockingLock {
            sink.value.writeString("${message.toAnsi().cleanString()}\n")
        }
    }

    protected open fun dispose() {
        sink.value.flush()
        sink.release { sinks -= path }
    }
}