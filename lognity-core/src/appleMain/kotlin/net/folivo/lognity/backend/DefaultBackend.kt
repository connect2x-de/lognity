package net.folivo.lognity.backend

import kotlinx.io.files.Path
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.appender.FileAppender
import net.folivo.lognity.appender.OsAppender

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