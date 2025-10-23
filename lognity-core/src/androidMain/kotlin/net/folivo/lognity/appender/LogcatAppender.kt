package net.folivo.lognity.appender

import android.util.Log
import net.folivo.lognity.api.ansi.toAnsi
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.util.logcatLevel

internal class LogcatAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter
) : Appender { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        Log.println(level.logcatLevel, marker?.name, message.toAnsi().cleanString())
    }
}