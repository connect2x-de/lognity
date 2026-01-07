package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.appender.NoopAppender
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.ExtendedConsoleAppender

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = ExtendedConsoleAppender(pattern, formatter, filter, name) // @formatter:on

internal actual fun createSystemFileAppender( // @formatter:off
    path: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = NoopAppender // @formatter:on

internal actual fun createSystemRollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = NoopAppender // @formatter:on