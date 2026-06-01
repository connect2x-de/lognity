@file:JvmName("DefaultBackendImpl")

package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.ConsoleColorScheme
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.ExtendedConsoleAppender

internal actual fun getConsoleColorScheme(): ConsoleColorScheme = ConsoleColorScheme.DARK

internal actual fun getOverrideLogLevel(): Level? { // @formatter:off
    return System.getenv("LOGNITY_DEFAULT_LEVEL")?.let(Level::byName)
        ?: System.getProperty("lognity.default.level")?.let(Level::byName)
} // @formatter:on

internal actual fun getDefaultLogLevel(): Level = getOverrideLogLevel() ?: Level.INFO

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender { // @formatter:on
    return ExtendedConsoleAppender(pattern, formatter, filter, name)
}

internal actual fun createSystemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender {
    return ExtendedConsoleAppender(pattern, formatter, filter, name) // @formatter:on
}
