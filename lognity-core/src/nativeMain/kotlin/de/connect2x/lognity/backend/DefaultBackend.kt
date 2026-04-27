package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.SynchronizedConsoleAppender
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.getenv
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@PublishedApi
internal actual fun getDefaultLogLevel(): Level { // @formatter:off
    return getenv("LOGNITY_DEFAULT_LEVEL")?.toKStringFromUtf8()?.let(Level::byName)
        ?: if (Platform.isDebugBinary) Level.DEBUG else Level.INFO
} // @formatter:on

internal actual fun createSystemConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String?
): Appender = SynchronizedConsoleAppender(pattern, formatter, filter, name) // @formatter:on