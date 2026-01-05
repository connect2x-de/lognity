package de.connect2x.lognity.backend

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.appender.LogcatAppender
import de.connect2x.lognity.api.backend.Platform as LognityPlatform

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
): Appender = LogcatAppender(pattern, formatter, filter) // @formatter:on

internal actual fun getCurrentPlatform(): LognityPlatform = LognityPlatform.ANDROID