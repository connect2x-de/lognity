package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.appender.RollingFileAppender
import de.connect2x.lognity.appender.SynchronizedConsoleAppender
import kotlinx.io.files.Path
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
@PublishedApi
internal actual fun getDefaultLogLevel(): Level = if (Platform.isDebugBinary) Level.DEBUG else Level.INFO

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
): Appender =
    RollingFileAppender(
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
    ) // @formatter:on

internal actual fun createSystemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = SynchronizedConsoleAppender(pattern, formatter, filter, name) // @formatter:on