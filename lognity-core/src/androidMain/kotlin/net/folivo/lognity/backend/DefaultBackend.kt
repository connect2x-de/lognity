@file:JvmName("DefaultBackendImpl")

package net.folivo.lognity.backend

import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.appender.LogcatAppender

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