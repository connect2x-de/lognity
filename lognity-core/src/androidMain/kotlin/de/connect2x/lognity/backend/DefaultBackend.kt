@file:JvmName("DefaultBackendImpl")

package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.LogcatAppender

internal actual fun getDefaultLogLevel(): Level {
    return System.getProperty("lognity.default.level")?.let { levelName ->
        Level.entries.find { it.name == levelName }
    } ?: Level.INFO
}

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
): Appender = LogcatAppender(pattern, formatter, filter) // @formatter:on