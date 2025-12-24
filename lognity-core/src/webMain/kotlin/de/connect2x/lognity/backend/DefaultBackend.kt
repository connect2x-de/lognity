@file:OptIn(ExperimentalWasmJsInterop::class)

package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.appender.NoopAppender
import de.connect2x.lognity.api.backend.Platform
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.ExtendedConsoleAppender
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toJsString

@PublishedApi
internal actual fun getDefaultLogLevel(): Level {
    val params = URLSearchParams(window.location.search.toJsString())
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

internal actual fun getCurrentPlatform(): Platform = Platform.WEB