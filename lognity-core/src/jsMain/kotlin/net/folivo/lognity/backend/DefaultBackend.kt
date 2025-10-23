package net.folivo.lognity.backend

import kotlinx.browser.window
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.appender.NoopAppender
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.appender.ExtendedConsoleAppender
import org.w3c.dom.url.URLSearchParams

@PublishedApi
internal actual fun getDefaultLogLevel(): Level {
    val params = URLSearchParams(window.location.search)
    val levelName = params.get("logLevel")
    return Level.entries.find { it.name.equals(levelName, true) } ?: Level.INFO
}

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
): Appender = ExtendedConsoleAppender(pattern, formatter, filter) // @formatter:on

internal actual fun createSystemFileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    path: String
): Appender = NoopAppender // @formatter:on