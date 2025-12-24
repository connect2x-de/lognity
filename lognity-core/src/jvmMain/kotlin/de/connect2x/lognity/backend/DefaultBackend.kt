@file:JvmName("DefaultBackendImpl")

package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.Platform
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.appender.ExtendedConsoleAppender
import oshi.PlatformEnum
import oshi.SystemInfo

internal actual fun getDefaultLogLevel(): Level {
    return System.getProperty("lognity.default.level")?.let { levelName ->
        Level.entries.find { it.name == levelName }
    } ?: Level.INFO
}

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
): Appender { // @formatter:on
    return ExtendedConsoleAppender(pattern, formatter, filter)
}

internal actual fun getCurrentPlatform(): Platform = when (SystemInfo.getCurrentPlatform()) {
    PlatformEnum.WINDOWS -> Platform.WINDOWS
    PlatformEnum.LINUX -> Platform.LINUX
    PlatformEnum.MACOS -> Platform.MACOS
    else -> Platform.UNKNOWN_JVM
}