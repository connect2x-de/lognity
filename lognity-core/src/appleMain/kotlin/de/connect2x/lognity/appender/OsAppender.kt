package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.ansi.toAnsi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.osLogType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_t

/**
 * Appender that writes log messages to the Apple unified logging system (os_log).
 *
 * It creates a separate `os_log_t` for each [Logger] using the logger's name as the category.
 * Lognity's [Level] is mapped to the corresponding `os_log_type_t`.
 *
 * @property pattern The formatting pattern string used by this appender.
 * @property formatter The formatter that produces the final message from [pattern].
 * @property filter A filter that decides whether a given message should be written.
 * @property name Optional name for this appender.
 */
class OsAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    override val name: String? = null
) : AbstractAppender() { // @formatter:on
    companion object {
        private const val CATEGORY: String = "general"
        private val delegates: SharedHashMap<Logger, os_log_t> = SharedHashMap()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        _os_log_internal(__dso_handle.ptr, delegates.getOrPut(logger) {
            val name = logger.context[Logger.Name]?.name ?: logger.toString()
            os_log_create(name, CATEGORY)
        }, level.osLogType, "%{public}s", message.toAnsi().cleanString())
    }
}