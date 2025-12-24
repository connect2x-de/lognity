package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.appender.NoopAppender
import de.connect2x.lognity.api.backend.Platform
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.ExtendedConsoleAppender

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