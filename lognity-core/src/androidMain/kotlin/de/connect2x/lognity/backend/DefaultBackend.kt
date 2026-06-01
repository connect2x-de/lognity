@file:JvmName("DefaultBackendImpl")

package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.ConsoleColorScheme
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.ConsoleAppender
import de.connect2x.lognity.appender.LogcatAppender

internal actual fun getConsoleColorScheme(): ConsoleColorScheme = ConsoleColorScheme.DARK

internal actual fun getOverrideLogLevel(): Level? {
    return System.getProperty("lognity.default.level")?.let { levelName ->
        Level.entries.find { it.name == levelName }
    }
}

internal actual fun getDefaultLogLevel(): Level = getOverrideLogLevel() ?: Level.INFO

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = LogcatAppender(pattern, formatter, filter, name) // @formatter:on

internal actual fun createSystemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = ConsoleAppender(pattern, formatter, filter, name) // @formatter:on
