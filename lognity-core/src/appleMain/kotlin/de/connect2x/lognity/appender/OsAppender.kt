package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.osLogType
import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_t

class OsAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    override val name: String? = null
) : Appender { // @formatter:on
    companion object {
        private val delegates: SharedHashMap<Logger, os_log_t> = SharedHashMap()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        _os_log_internal(null, delegates.getOrPut(logger) {
            val name = logger.context[Logger.Name]?.name ?: logger.toString()
            os_log_create(name, null)
        }, level.osLogType, message)
    }
}