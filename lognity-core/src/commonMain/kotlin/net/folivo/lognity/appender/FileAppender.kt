package net.folivo.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import net.folivo.lognity.api.ansi.toAnsi
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.backend.withBlockingLock
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
private data class RefCountedSink(
    val value: Sink
) {
    private var refCount: AtomicInt = AtomicInt(0)

    @Suppress("NOTHING_TO_INLINE")
    inline fun acquire(): RefCountedSink {
        refCount.incrementAndFetch()
        return this
    }

    inline fun release(releaseAction: () -> Unit = {}): RefCountedSink {
        if (refCount.load() == 0) return this
        if (refCount.decrementAndFetch() == 0) {
            releaseAction()
            value.close()
            return this
        }
        return this
    }
}

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
    val path: Path
) : Appender { // @formatter:on
    companion object {
        private val sinks: SharedHashMap<Path, RefCountedSink> = SharedHashMap()
    }

    init {
        Backend.addShutdownHook(::dispose)
    }

    private val sink: RefCountedSink = sinks.getOrPut(path) {
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

    private fun dispose() {
        sink.value.flush()
        sink.release { sinks -= path }
    }
}