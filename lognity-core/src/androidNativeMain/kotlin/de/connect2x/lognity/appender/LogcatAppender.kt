package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.logcatLevel
import platform.android.__android_log_write

/**
 * Appender that writes log messages to Android's Logcat.
 *
 * It maps Lognity's [Level] to Android's `__android_log_write` levels and uses the [Marker] name
 * (if present) as the Logcat tag. ANSI escape sequences are stripped from the
 * message before writing.
 *
 * @property pattern The formatting pattern string used by this appender.
 * @property formatter The formatter that produces the final message from [pattern].
 * @property filter A filter that decides whether a given message should be written.
 * @property name Optional name for this appender.
 */
class LogcatAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    override val name: String? = null
) : Appender { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || !filter(logger, message, marker)) return
        __android_log_write(level.logcatLevel, marker?.name, message.toAnsi().cleanString())
    }
}