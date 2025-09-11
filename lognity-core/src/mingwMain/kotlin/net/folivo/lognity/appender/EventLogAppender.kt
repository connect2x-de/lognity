/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.appender

import co.touchlab.stately.collections.ConcurrentMutableMap
import net.folivo.lognity.api.LogLevel
import net.folivo.lognity.api.LogMarker
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.format.LogFormatter
import net.folivo.lognity.util.eventType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import net.folivo.lognity.api.appender.LogAppender
import net.folivo.lognity.api.appender.LogFilter
import platform.windows.DeregisterEventSource
import platform.windows.HANDLE
import platform.windows.RegisterEventSourceW
import platform.windows.ReportEventW
import kotlin.collections.iterator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalForeignApi::class)
internal class EventLogAppender( // @formatter:off
    override val pattern: String,
    override val formatter: LogFormatter,
    private val filter: LogFilter
) : LogAppender { // @formatter:on
    private val eventSources: ConcurrentMutableMap<Logger, HANDLE> = ConcurrentMutableMap()

    private fun getOrCreateEventSource(logger: Logger): HANDLE {
        return eventSources.getOrPut(logger) {
            requireNotNull(RegisterEventSourceW(null, logger.name)) {
                "Could not create event source for logger $logger"
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun append(logger: Logger, level: LogLevel, message: String, marker: LogMarker?) = memScoped {
        if (!filter(level, message, marker)) return@memScoped
        val eventSource = getOrCreateEventSource(logger)
        val eventId = Uuid.random().hashCode().toUInt()
        ReportEventW(eventSource, level.eventType, 0U, eventId, null, 1U, 0U, cValuesOf(message.wcstr.ptr), null)
    }

    override fun dispose() {
        for ((_, handle) in eventSources) {
            DeregisterEventSource(handle)
        }
    }
}