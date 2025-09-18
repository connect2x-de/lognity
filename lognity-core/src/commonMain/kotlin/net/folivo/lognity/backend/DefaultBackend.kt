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

package net.folivo.lognity.backend

import kotlinx.io.files.Path
import net.folivo.lognity.DefaultLogger
import net.folivo.lognity.DefaultMarker
import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.appender.ConsoleAppender
import net.folivo.lognity.appender.FileAppender
import net.folivo.lognity.format.DefaultFormatter
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.ExperimentalTime

internal expect fun getDefaultLogLevel(): Level

internal expect fun createSystemLogAppender( // @formatter:off
    pattern: String,
    formatter: Formatter = Formatter.default,
    filter: Filter = Filter.always
): Appender // @formatter:on

// TODO: document this
@OptIn(ExperimentalAtomicApi::class)
object DefaultBackend : Backend {
    override val name: String = "Skroll"
    override val defaultLevel: Level = getDefaultLogLevel()

    private val _defaultConfigSpec: AtomicReference<ConfigBuilder.() -> Unit> = AtomicReference {
        platformConsoleAppender(
            "{{levelColor}}>>  {{levelSymbol}}\t{{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{thread}}) {{message}}{{r}}"
        )
        fileAppender(
            pattern = "[{{level}}][{{yyyy}}/{{MM}}/{{dd}} {{hh}}:{{mm}}:{{ss}}.{{SSS}}] ({{name}} @ {{thread}}) {{message}}",
            path = Path("latest.log"),
            filter = Filter.levelsExcept(Level.DEBUG, Level.TRACE)
        )
        fileAppender(
            pattern = "[{{level}}][{{yyyy}}/{{MM}}/{{dd}} {{hh}}:{{mm}}:{{ss}}.{{SSS}}] ({{name}} @ {{thread}}) {{message}}",
            path = Path("debug.log"),
            filter = Filter.levels(Level.DEBUG)
        )
    }

    override var defaultConfigSpec: ConfigBuilder.() -> Unit
        get() = _defaultConfigSpec.load()
        set(value) {
            _defaultConfigSpec.store(value)
        }

    @OptIn(ExperimentalTime::class)
    override val defaultFormatter: Formatter get() = DefaultFormatter

    override fun createMarker(
        key: String, name: String, isEnabled: Boolean
    ): Marker {
        return DefaultMarker(key, name, isEnabled)
    }

    override fun createLogger(
        name: String, configSpec: ConfigBuilder.() -> Unit
    ): Logger {
        return DefaultLogger(name, ConfigBuilder().apply(configSpec).build())
    }

    override fun createFileAppender(
        pattern: String, formatter: Formatter, filter: Filter, path: Path
    ): Appender {
        return FileAppender(pattern, formatter, path, filter)
    }

    override fun createConsoleAppender(
        pattern: String, formatter: Formatter, filter: Filter
    ): Appender {
        return ConsoleAppender(pattern, formatter, filter)
    }

    override fun createSystemAppender(
        pattern: String, formatter: Formatter, filter: Filter
    ): Appender {
        return createSystemLogAppender(pattern, formatter, filter)
    }
}