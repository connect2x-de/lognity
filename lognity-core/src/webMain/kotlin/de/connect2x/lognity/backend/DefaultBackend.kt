package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.appender.NoopAppender
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.ExtendedConsoleAppender
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.appender.RollingFileAppender
import de.connect2x.lognity.util.isNode
import kotlinx.io.files.Path

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

internal actual fun createSystemFileAppender( // @formatter:off
    path: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = if (isNode) FileAppender(pattern, formatter, filter, Path(path), name) else NoopAppender // @formatter:on

internal actual fun createSystemRollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?,
    fileCount: Int,
    maxFileSize: Long,
    useTimestamps: Boolean
): Appender = if(isNode) RollingFileAppender(pattern, formatter, filter, Path(basePath), name, fileCount, maxFileSize, useTimestamps) else NoopAppender // @formatter:on