@file:JvmName("ConfigBuilderExtensions$")

package de.connect2x.lognity.config

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.appender.RollingFileAppender
import kotlin.jvm.JvmName
import kotlinx.io.files.Path

/**
 * Adds a new file appender to this logger config.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param path The file path at which to save the log.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 * @param name The name of the appender.
 * @param deleteExisting Whether to delete the existing log file at [path] on startup.
 */
fun ConfigBuilder.fileAppender( // @formatter:off
    path: String,
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null,
    deleteExisting: Boolean = false
) { // @formatter:on
    appender(FileAppender(pattern, formatter, filter, Path(path), name, deleteExisting))
}

/**
 * Adds a new rolling file appender to this logger config.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param basePath The base file path at which to save the logs.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 * @param name The name of the appender.
 * @param maxFileCount The maximum number of log files to keep.
 * @param maxFileSize The maximum size in bytes for a single log file before it is rolled.
 * @param useTimestamps Whether to include timestamps in the rolled file names.
 * @param deleteExisting Whether to delete existing log files matching the pattern on startup.
 * @param latestSuffix The suffix used for the current active log file.
 */
fun ConfigBuilder.rollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null,
    maxFileCount: Int = RollingFileAppender.DEFAULT_FILE_COUNT,
    maxFileSize: Long = RollingFileAppender.DEFAULT_FILE_SIZE,
    useTimestamps: Boolean = true,
    deleteExisting: Boolean = false,
    latestSuffix: String = RollingFileAppender.DEFAULT_LATEST_SUFFIX
) { // @formatter:on
    appender(
        RollingFileAppender(
            pattern,
            formatter,
            filter,
            Path(basePath),
            name,
            maxFileCount,
            maxFileSize,
            useTimestamps,
            deleteExisting,
            latestSuffix,
        ),
    )
}
