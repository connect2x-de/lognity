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

import co.touchlab.stately.collections.SharedHashMap
import kotlinx.cinterop.ExperimentalForeignApi
import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
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