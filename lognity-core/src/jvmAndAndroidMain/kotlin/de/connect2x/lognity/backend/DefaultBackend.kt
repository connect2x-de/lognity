package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.appender.RollingFileAppender
import kotlinx.io.files.Path

internal actual fun createSystemFileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    path: String
): Appender = FileAppender(pattern, formatter, filter, Path(path)) // @formatter:on

internal actual fun createSystemRollingFileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    basePath: String
): Appender = RollingFileAppender(pattern, formatter, filter, Path(basePath)) // @formatter:on