package de.connect2x.lognity.config

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.config.ConfigDsl
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.ConsoleAppender
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
 */
@ConfigDsl
fun ConfigBuilder.rollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    name: String? = null,
    maxFileCount: Int = 10,
    maxFileSize: Long = 1024 * 1024, // 1MB
    useTimestamps: Boolean = true,
    deleteExisting: Boolean = false
) { // @formatter:on
    appender(
        createSystemRollingFileAppender(
            basePath, pattern, formatter, filter, name, maxFileCount, maxFileSize, useTimestamps, deleteExisting
        )
    )
}