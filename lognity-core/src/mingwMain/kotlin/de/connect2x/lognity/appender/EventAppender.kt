package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.appender.Appender
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalForeignApi::class)
class EventAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    override val filter: Filter
) : Appender { // @formatter:on
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

    @OptIn(ExperimentalUuidApi::class)
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) = memScoped {
        if (!filter(level, message, marker)) return@memScoped
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