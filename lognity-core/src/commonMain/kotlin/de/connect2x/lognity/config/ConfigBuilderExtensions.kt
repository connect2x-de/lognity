package de.connect2x.lognity.config

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.config.ConfigDsl
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.ConsoleAppender
import de.connect2x.lognity.appender.RollingFileAppender
import de.connect2x.lognity.backend.createSystemConsoleAppender
import de.connect2x.lognity.backend.createSystemFileAppender
import de.connect2x.lognity.backend.createSystemLogAppender
import de.connect2x.lognity.backend.createSystemRollingFileAppender

/**
 * Adds a new console appender to this logger config.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 * @param name The name of the appender.
 */
@ConfigDsl
fun ConfigBuilder.consoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null
) { // @formatter:on
    appender(ConsoleAppender(pattern, formatter, filter, name))
}

/**
 * Adds a new system log appender to this logger config.
 * This will use the default log appender for the underlying platform.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 * @param name The name of the appender.
 */
@ConfigDsl
fun ConfigBuilder.systemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null
) { // @formatter:on
    appender(createSystemLogAppender(pattern, formatter, filter, name))
}

/**
 * Adds a new system console appender to this logger config.
 * This will use the default console appender for the underlying platform.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 * @param name The name of the appender.
 */
@ConfigDsl
fun ConfigBuilder.systemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null
) { // @formatter:on
    appender(createSystemConsoleAppender(pattern, formatter, filter, name))
}

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
@ConfigDsl
fun ConfigBuilder.fileAppender( // @formatter:off
    path: String,
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null,
    deleteExisting: Boolean = false
) { // @formatter:on
    appender(createSystemFileAppender(path, pattern, formatter, filter, name, deleteExisting))
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
@ConfigDsl
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
        createSystemRollingFileAppender(
            basePath,
            pattern,
            formatter,
            filter,
            name,
            maxFileCount,
            maxFileSize,
            useTimestamps,
            deleteExisting,
            latestSuffix
        )
    )
}