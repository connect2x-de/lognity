package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.appender.RollingFileAppender
import kotlinx.io.files.Path

internal actual fun createSystemFileAppender( // @formatter:off
    path: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?,
    deleteExisting: Boolean
): Appender = FileAppender(pattern, formatter, filter, Path(path), name, deleteExisting) // @formatter:on

internal actual fun createSystemRollingFileAppender( // @formatter:off
    basePath: String,
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?,
    fileCount: Int,
    maxFileSize: Long,
    useTimestamps: Boolean,
    deleteExisting: Boolean,
    latestSuffix: String
): Appender { // @formatter:on
    return RollingFileAppender(
        pattern,
        formatter,
        filter,
        Path(basePath),
        name,
        fileCount,
        maxFileSize,
        useTimestamps,
        deleteExisting,
        latestSuffix
    )
}