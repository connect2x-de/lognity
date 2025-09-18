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

package net.folivo.lognity.format

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.ansi.AnsiSequence
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.backend.getThreadId
import net.folivo.lognity.backend.getThreadName
import net.folivo.lognity.util.ThreadLocal
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal object DefaultFormatter : Formatter {
    private val maxLevelNameLength: Int = Level.entries //
        .maxOf { it.name.length }

    private val paddedLevelNames: Array<String> = Level.entries //
        .map { it.name.padEnd(maxLevelNameLength, '-') } //
        .toTypedArray()

    private val yearFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { year() }
    private val monthFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { monthNumber() }
    private val dayFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { day() }
    private val hourFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { hour() }
    private val minuteFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { minute() }
    private val secondFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { second() }
    private val secondFractionFormat: DateTimeFormat<DateTimeComponents> =
        DateTimeComponents.Format { secondFraction() }

    @OptIn(ExperimentalTime::class)
    private val defaultFormatterFst: FST<FormatterContext> = FST(mapOf( // @formatter:off
        "r" to { AnsiSequence.reset.toString() },
        "levelColor" to { ctx -> ctx.level.ansi.toString() },
        "marker" to { ctx -> ctx.marker?.name ?: "" },
        "message" to { ctx -> ctx.content.toString() },
        "thread" to { getThreadName() },
        "threadId" to { getThreadId().toString() },
        "level" to { ctx -> paddedLevelNames[ctx.level.ordinal] },
        "levelSymbol" to { ctx -> ctx.level.symbol },
        "name" to { ctx -> ctx.logger.name },
        "yyyy" to { Clock.System.now().format(yearFormat) },
        "MM" to { Clock.System.now().format(monthFormat) },
        "dd" to { Clock.System.now().format(dayFormat) },
        "hh" to { Clock.System.now().format(hourFormat) },
        "mm" to { Clock.System.now().format(minuteFormat) },
        "ss" to { Clock.System.now().format(secondFormat) },
        "SSS" to { Clock.System.now().format(secondFractionFormat) }
    )) // @formatter:on

    private val formatterContext: ThreadLocal<FormatterContext> = ThreadLocal { FormatterContext() }

    override operator fun invoke(logger: Logger, level: Level, content: Any, marker: Marker?, s: String): String {
        return defaultFormatterFst(s, formatterContext.get().apply {
            this.logger = logger
            this.level = level
            this.content = content
            this.marker = marker
        })
    }
}