package net.folivo.lognity.config

import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.appender.ConsoleAppender
import net.folivo.lognity.backend.createSystemFileAppender
import net.folivo.lognity.backend.createSystemLogAppender

/**
 * Adds a new console appender to this logger config.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 */
fun ConfigBuilder.consoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always
) { // @formatter:on
    appender(ConsoleAppender(pattern, formatter, filter))
}

/**
 * Adds a new platform console appender to this logger config.
 * This will automatically use the appropriate console appender for the
 * underlying platform instead of raw stdio if available.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 */
fun ConfigBuilder.platformConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always
) { // @formatter:on
    appender(createSystemLogAppender(pattern, formatter, filter))
}

/**
 * Adds a new file appender to this logger config.
 *
 * @param pattern The formatting pattern to apply to all messages passed to the new appender.
 * @param path The file path at which to save the log.
 * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
 * @param filter The filter to apply for every message to determine whether it should be logged.
 */
fun ConfigBuilder.fileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always,
    path: String
) { // @formatter:on
    appender(createSystemFileAppender(pattern, formatter, filter, path))
}