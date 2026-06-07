package de.connect2x.lognity.api.appender

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

/**
 * A fake appender which allows defining name, pattern and formatter,
 * for use when no real appender is available when invoking [Formatter].
 */
class FakeAppender( // @formatter:off
    override val name: String? = null,
    override val pattern: String = "{{message}}",
    override val formatter: Formatter = Formatter.default
) : Appender { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) = Unit

    @ExperimentalLoggingApi
    override suspend fun appendSuspend(logger: Logger, level: Level, message: String, marker: Marker?) = Unit
}