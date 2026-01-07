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

class LogcatAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    override val name: String? = null
) : Appender { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        __android_log_write(level.logcatLevel, marker?.name, message.toAnsi().cleanString())
    }
}