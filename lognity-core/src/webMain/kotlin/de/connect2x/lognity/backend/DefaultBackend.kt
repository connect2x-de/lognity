@file:OptIn(ExperimentalWasmJsInterop::class)

package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.ConsoleColorScheme
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.ExtendedConsoleAppender
import de.connect2x.lognity.util.getProcess
import de.connect2x.lognity.util.isDarkColorScheme
import de.connect2x.lognity.util.isNode
import js.string.JsStrings.toKotlinString
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toJsString
import web.url.URLSearchParams
import web.window.window

internal actual fun getConsoleColorScheme(): ConsoleColorScheme =
    if (isDarkColorScheme) ConsoleColorScheme.DARK else ConsoleColorScheme.LIGHT

internal actual fun getOverrideLogLevel(): Level? {
    if (isNode) {
        val levelName = getProcess().env.LOGNITY_DEFAULT_LEVEL ?: return null
        return Level.entries.find { it.name.equals(levelName, true) }
    }
    val params = URLSearchParams(window.location.search)
    val levelName = params.get("logLevel".toJsString())?.toKotlinString()
    return Level.entries.find { it.name.equals(levelName, true) }
}

internal actual fun getDefaultLogLevel(): Level = getOverrideLogLevel() ?: Level.INFO

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = ExtendedConsoleAppender(pattern, formatter, filter, name) // @formatter:on

internal actual fun createSystemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = ExtendedConsoleAppender(pattern, formatter, filter, name) // @formatter:on
