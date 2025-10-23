package net.folivo.lognity.backend

import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.appender.NoopAppender
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.appender.ConsoleAppender
import kotlin.experimental.ExperimentalNativeApi

// TODO: Make this configurable
@OptIn(ExperimentalNativeApi::class)
@PublishedApi
internal actual fun getDefaultLogLevel(): Level = Level.INFO

internal actual fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter
): Appender = ConsoleAppender(pattern, formatter, filter) // @formatter:on

internal actual fun createSystemFileAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    path: String
): Appender = NoopAppender // @formatter:on