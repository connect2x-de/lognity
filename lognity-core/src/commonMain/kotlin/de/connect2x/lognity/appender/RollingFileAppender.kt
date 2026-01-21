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

/**
 * An appender that writes log messages to files with rolling capabilities.
 *
 * @property pattern The pattern used for formatting log messages.
 * @property formatter The formatter used to format log events.
 * @property filter The filter used to decide whether to log a message.
 * @property basePath The base path to the log files.
 * @property name The optional name of the appender.
 * @param fileCount The maximum number of log files to keep.
 * @param maxFileSize The maximum size in bytes for a single log file.
 * @param useTimestamps If true, timestamps will be used in log file names.
 * @param deleteExisting If true, any existing files at the given path will be deleted upon initialization.
 * @param latestSuffix The suffix used for the current active log file.
 */
@OptIn(ExperimentalAtomicApi::class)
class RollingFileAppender(
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    val basePath: Path,
    override val name: String? = null,
    fileCount: Int = DEFAULT_FILE_COUNT,
    maxFileSize: Long = DEFAULT_FILE_SIZE,
    useTimestamps: Boolean = true,
    deleteExisting: Boolean = false,
    latestSuffix: String = DEFAULT_LATEST_SUFFIX
) : Appender {
    companion object {
        /**
         * Default maximum number of log files to keep.
         */
        const val DEFAULT_FILE_COUNT: Int = 8

        /**
         * Default maximum size in bytes for a single log file (10MB).
         */
        const val DEFAULT_FILE_SIZE: Long = 1024 * 1024 * 10 // 10MB

        /**
         * Default suffix used for the current active log file.
         */
        const val DEFAULT_LATEST_SUFFIX: String = "-latest"
    }

    /**
     * The asynchronous sink used for writing to files with rolling capabilities.
     */
    val sink: RollingAsyncSink =
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