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

package net.folivo.lognity.api.backend

import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.config.ConfigBuilder
import kotlinx.io.files.Path
import net.folivo.lognity.api.format.Formatter

private object NoopMarker : Marker {
    override val key: String = ""
    override val name: String = ""

    override var isEnabled: Boolean
        get() = false
        set(value) {}
}

private object NoopLogger : Logger {
    override val name: String = ""
    override val config: Config = Config()

    override var level: Level
        get() = Level.INFO
        set(value) {}

    override var isEnabled: Boolean
        get() = false
        set(value) {}

    override fun log(level: Level, message: AnsiScope.() -> Any) = Unit
    override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any) = Unit
}

private object NoopAppender : Appender {
    override val formatter: Formatter = Formatter.identity
    override val pattern: String = ""

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) = Unit
}

/**
 * A no-operation implementation of the [Backend] interface.
 * This implementation doesn't perform any actual logging operations and is useful
 * for situations where logging should be completely disabled or as a default
 * implementation when no other backend is configured.
 */
object NoopBackend : Backend {
    override val name: String = "NOOP"
    override val defaultLevel: Level = Level.WARN
    override val defaultFormatter: Formatter = Formatter.identity

    override var defaultConfigSpec: ConfigBuilder.() -> Unit
        get() = {}
        set(value) {}

    override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker = NoopMarker

    override fun createLogger(name: String, configSpec: ConfigBuilder.() -> Unit): Logger = NoopLogger

    override fun createFileAppender(
        pattern: String, formatter: Formatter, filter: Filter, path: Path
    ): Appender = NoopAppender

    override fun createConsoleAppender(
        pattern: String, formatter: Formatter, filter: Filter
    ): Appender = NoopAppender

    override fun createSystemAppender(
        pattern: String, formatter: Formatter, filter: Filter
    ): Appender = NoopAppender
}
