package net.folivo.lognity.api.appender

import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker

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