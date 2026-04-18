package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.eventType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.windows.DeregisterEventSource
import platform.windows.HANDLE
import platform.windows.RegisterEventSourceW
import platform.windows.ReportEventW
import kotlin.uuid.Uuid

/**
 * Appender that writes log messages to the Windows Event Log.
 *
 * It maps Lognity's [Level] to Windows event types and uses the [Logger] name as the event source.
 * Each log message is reported as a separate event in the Windows Event Log.
 *
 * @property pattern The formatting pattern string used by this appender.
 * @property formatter The formatter that produces the final message from [pattern].
 * @property filter A filter that decides whether a given message should be written.
 * @property name Optional name for this appender.
 */
@OptIn(ExperimentalForeignApi::class)
class EventAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter,
    override val name: String? = null
) : AbstractAppender() { // @formatter:on
    private val eventSources: SharedHashMap<Logger, HANDLE> = SharedHashMap()

    init {
        Backend.addShutdownHook(::dispose)
    }

    private fun getOrCreateEventSource(logger: Logger): HANDLE {
        return eventSources.getOrPut(logger) {
            val name = logger.context[Logger.Name]?.name ?: logger.toString()
            requireNotNull(RegisterEventSourceW(null, name)) {
                "Could not create event source for logger $logger"
            }
        }
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) = memScoped {
        if (message.isEmpty() || !filter(logger, level, message, marker)) return@memScoped
        val eventSource = getOrCreateEventSource(logger)
        val eventId = Uuid.random().hashCode().toUInt()
        ReportEventW(eventSource, level.eventType, 0U, eventId, null, 1U, 0U, cValuesOf(message.wcstr.ptr), null)
    }

    private fun dispose() {
        for ((_, handle) in eventSources) {
            DeregisterEventSource(handle)
        }
    }
}