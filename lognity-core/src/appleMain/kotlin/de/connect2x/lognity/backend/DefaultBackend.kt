package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.appender.OsAppender
import kotlinx.io.files.Path

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
): Appender = OsAppender(pattern, formatter, filter) // @formatter:on

internal actual fun createSystemFileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    path: String
): Appender = FileAppender(pattern, formatter, filter, Path(path)) // @formatter:on