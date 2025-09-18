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

import co.touchlab.stately.collections.SharedHashMap
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.char
import kotlinx.io.files.Path
import net.folivo.lognity.DefaultLogger
import net.folivo.lognity.DefaultMarker
import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.ansi.AnsiSequence
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.format.FormatElement
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.appender.ConsoleAppender
import net.folivo.lognity.appender.FileAppender
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private typealias DateTimeElement = Pair<String, DateTimeFormatBuilder.WithDateTimeComponents.() -> Unit>

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
            "{{levelColor}}>>  {{levelSymbol}}\t{{datetime(hh:mm:ss.SSS)}} ({{name}} @ {{thread}}) {{message}}{{r}}"
        )
        fileAppender(
            pattern = "[{{level}}][{{datetime(yyyy/MM/dd hh:mm:ss.SSS)}}] ({{name}} @ {{thread}}) {{message}}",
            path = Path("latest.log"),
            filter = Filter.levelsExcept(Level.DEBUG, Level.TRACE)
        )
        fileAppender(
            pattern = "[{{level}}][{{datetime(yyyy/MM/dd hh:mm:ss.SSS)}}] ({{name}} @ {{thread}}) {{message}}",
            path = Path("debug.log"),
            filter = Filter.levels(Level.DEBUG)
        )
    }
    override var defaultConfigSpec: ConfigBuilder.() -> Unit
        get() = _defaultConfigSpec.load()
        set(value) {
            _defaultConfigSpec.store(value)
        }

    private val maxLevelNameLength: Int = Level.entries //
        .maxOf { it.name.length }

    private val paddedLevelNames: Array<String> = Level.entries //
        .map { it.name.padEnd(maxLevelNameLength, '-') } //
        .toTypedArray()

    private val dateTimeElements: List<DateTimeElement> = listOf( // @formatter:off
        "yyyy" to { year() },
        "yy" to { yearTwoDigits(2000) },
        "MM" to { monthNumber() },
        "dd" to { day() },
        "hh" to { amPmHour() },
        "HH" to { hour() },
        "mm" to { minute() },
        "ss" to { second() },
        "SSS" to { secondFraction(3) }
    ) // @formatter:on

    private val dateTimeFormatCache: SharedHashMap<String, DateTimeFormat<DateTimeComponents>> = SharedHashMap(1)

    private fun String.replaceTemplate(name: String, value: String): String {
        return replace("{{$name}}", value)
    }

    private inline fun String.replaceTemplate(name: String, transform: (String) -> String): String {
        var result = this
        val matchString = "{{$name("
        val matchLength = matchString.length
        while (true) {
            val matchBegin = result.indexOf(matchString)
            if (matchBegin == -1) break
            val paramsBegin = matchBegin + matchLength
            val paramsEnd = result.indexOf(')', paramsBegin)
            val params = result.substring(paramsBegin, paramsEnd)
            // Match begin + length of ident match + length of params + 3 for delimiter ')}}'
            val matchEnd = matchBegin + matchLength + (paramsEnd - paramsBegin) + 3
            result = result.replaceRange(matchBegin, matchEnd, transform(params))
        }
        return result
    }

    private fun String.toDateTimeFormat(): DateTimeFormat<DateTimeComponents> {
        return dateTimeFormatCache.getOrPut(this) {
            DateTimeComponents.Format {
                // Slide from left to right and add chars and components as we go
                var skip = 0
                outer@ for (charIndex in indices) {
                    if (skip > 0) { // Skip any next chars from previous iteration
                        skip--
                        continue
                    }
                    // Try to match one of the elements from the current char index
                    for (element in dateTimeElements) {
                        if (!this@toDateTimeFormat.startsWith(element.first, charIndex)) continue
                        element.second(this)
                        skip = element.first.length - 1 // Skip n chars until end of interpolation variable
                        continue@outer
                    }
                    char(this@toDateTimeFormat[charIndex]) // Add regular character
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override val defaultFormatter: Formatter = FormatElement { logger, level, content, marker, s ->
        s.replaceTemplate("r", AnsiSequence.reset.toString())
            .replaceTemplate("levelColor", level.ansi.toString())
            .replaceTemplate("marker", marker?.name ?: "n/a")
            .replaceTemplate("message", content.toString())
            .replaceTemplate("thread", getThreadName())
            .replaceTemplate("threadId", getThreadId().toString())
            .replaceTemplate("level", paddedLevelNames[level.ordinal])
            .replaceTemplate("levelSymbol", level.symbol)
            .replaceTemplate("name", logger.name)
            .replaceTemplate("datetime") { Clock.System.now().format(it.toDateTimeFormat()) }
    }.asFormatter()

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