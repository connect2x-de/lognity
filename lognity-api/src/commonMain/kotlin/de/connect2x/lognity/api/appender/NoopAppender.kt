package de.connect2x.lognity.api.appender

import de.connect2x.lognity.api.appender.NoopAppender.append
import de.connect2x.lognity.api.appender.NoopAppender.formatter
import de.connect2x.lognity.api.appender.NoopAppender.pattern
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * A no-operation implementation of [Appender] that silently discards all log output.
 *
 * This is useful when you want to disable logging entirely (for tests, benchmarks,
 * or embedded environments) or as a placeholder when no real output target is desired.
 *
 * Behavior summary:
 * - [formatter] is set to [Formatter.identity], i.e., it does not transform messages.
 * - [pattern] is an empty string because no formatting/output is performed.
 * - [append] performs no action and immediately returns.
 */
object NoopAppender : Appender {
    override val formatter: Formatter = Formatter.identity
    override val pattern: String = ""

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) = Unit
}