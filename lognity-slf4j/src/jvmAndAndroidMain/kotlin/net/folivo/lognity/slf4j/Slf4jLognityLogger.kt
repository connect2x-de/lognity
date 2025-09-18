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

package net.folivo.lognity.slf4j

import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.config.ConfigBuilder
import org.slf4j.Logger as Slf4jLogger

class Slf4jLognityLogger(
    val delegate: Slf4jLogger
) : Logger {
    override val name: String get() = delegate.name
    override val config: Config = ConfigBuilder().apply(Backend.current.defaultConfigSpec).build()
    override var level: Level = Backend.current.defaultLevel
    override var isEnabled: Boolean = true

    private val formatPattern = config.appenders.first().pattern // Yank the first available log pattern for interop

    override fun log(level: Level, message: AnsiScope.() -> Any) = log(null, level, message)

    override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any) {
        if (level < this.level || marker?.isEnabled == false) return
        val messageContent = message(AnsiScope)
        val formattedMessage =
            Backend.current.defaultFormatter.transform(this, level, messageContent, marker, formatPattern)
        when (level) {
            Level.TRACE -> delegate.trace(formattedMessage)
            Level.DEBUG -> delegate.debug(formattedMessage)
            Level.INFO -> delegate.info(formattedMessage)
            Level.WARN -> delegate.warn(formattedMessage)
            Level.ERROR, Level.FATAL -> delegate.error(formattedMessage)
        }
    }
}

fun Slf4jLogger.asLognityLogger(): Logger = when (this) {
    is LognitySlf4jLogger -> delegate
    else -> Slf4jLognityLogger(this)
}