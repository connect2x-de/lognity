package net.folivo.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import kotlinx.cinterop.ExperimentalForeignApi
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.util.osLogType
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_t

internal class OsAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter
) : Appender { // @formatter:on
    companion object {
        private val delegates: SharedHashMap<Logger, os_log_t> = SharedHashMap()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        _os_log_internal(null, delegates.getOrPut(logger) {
            os_log_create(logger.name, null)
        }, level.osLogType, message)
    }
}