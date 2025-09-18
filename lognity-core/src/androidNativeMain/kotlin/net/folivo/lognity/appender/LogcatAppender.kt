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

import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.ansi.toAnsi
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.util.logcatLevel
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import platform.android.__android_log_write

internal class LogcatAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    private val filter: Filter
) : Appender { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        __android_log_write(level.logcatLevel, marker?.name, message.toAnsi().cleanString())
    }
}