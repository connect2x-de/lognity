package net.folivo.lognity.format

import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.logger.NoopLogger
import net.folivo.lognity.api.marker.Marker

/**
 * Context object provided to formatters when rendering log messages.
 *
 * It bundles together commonly used data about the logging event so that
 * formatter implementations can produce strings without having to reach back
 * into the logger or framework.
 *
 * Typical usage: a [CompiledFormat] can be created for [FormatterContext] and
 * later invoked with an instance of this class to render a message.
 *
 * @property logger The originating [Logger]. Defaults to [NoopLogger] as a safe placeholder.
 * @property level The effective log [Level] for this event. Defaults to [Level.default].
 * @property content The log message content or payload. Often a [String] but may be any type.
 * @property marker Optional [Marker] attached to the event for additional context or routing.
 */
data class FormatterContext( // @formatter:off
    var logger: Logger = NoopLogger,
    var level: Level = Level.default(),
    var content: Any = "",
    var marker: Marker? = null
) // @formatter:on