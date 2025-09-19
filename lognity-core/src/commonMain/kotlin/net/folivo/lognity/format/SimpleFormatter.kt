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

import co.touchlab.stately.collections.SharedHashMap
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

class SimpleFormatter(
    private val variables: Map<String, CompiledFormat.Segment<FormatterContext>>
) : Formatter {
    companion object {
        private val context: ThreadLocal<FormatterContext> = ThreadLocal { FormatterContext() }

        private val maxLevelNameLength: Int = Level.entries //
            .maxOf { it.name.length }

        private val paddedLevelNames: Array<String> = Level.entries //
            .map { it.name.padEnd(maxLevelNameLength, '-') } //
            .toTypedArray()

        internal val yearFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { year() }
        internal val monthFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { monthNumber() }
        internal val dayFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { day() }
        internal val hourFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { hour() }
        internal val minuteFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { minute() }
        internal val secondFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format { second() }
        internal val secondFractionFormat: DateTimeFormat<DateTimeComponents> =
            DateTimeComponents.Format { secondFraction(3, 3) }

        @OptIn(ExperimentalTime::class)
        val default: SimpleFormatter = SimpleFormatter(mapOf( // @formatter:off
            "r" to CompiledFormat.Text(AnsiSequence.reset.toString()),
            "levelColor" to CompiledFormat.Variable { ctx -> ctx.level.ansi.toString() },
            "marker" to CompiledFormat.Variable { ctx -> ctx.marker?.name ?: "" },
            "message" to CompiledFormat.Variable { ctx -> ctx.content.toString() },
            "thread" to CompiledFormat.Variable { getThreadName() },
            "threadId" to CompiledFormat.Variable { getThreadId().toString() },
            "level" to CompiledFormat.Variable { ctx -> paddedLevelNames[ctx.level.ordinal] },
            "levelSymbol" to CompiledFormat.Variable { ctx -> ctx.level.symbol },
            "name" to CompiledFormat.Variable { ctx -> ctx.logger.name },
            "yyyy" to CompiledFormat.Variable { Clock.System.now().format(yearFormat) },
            "MM" to CompiledFormat.Variable { Clock.System.now().format(monthFormat) },
            "dd" to CompiledFormat.Variable { Clock.System.now().format(dayFormat) },
            "hh" to CompiledFormat.Variable { Clock.System.now().format(hourFormat) },
            "mm" to CompiledFormat.Variable { Clock.System.now().format(minuteFormat) },
            "ss" to CompiledFormat.Variable { Clock.System.now().format(secondFormat) },
            "SSS" to CompiledFormat.Variable { Clock.System.now().format(secondFractionFormat) }
        )) // @formatter:on
    }

    private val formats: SharedHashMap<String, CompiledFormat<FormatterContext>> = SharedHashMap()

    override operator fun invoke(logger: Logger, level: Level, content: Any, marker: Marker?, s: String): String {
        val format = formats.getOrPut(s) { CompiledFormat.compile(variables, s) }
        return format(context.get().apply {
            this.logger = logger
            this.level = level
            this.content = content
            this.marker = marker
        })
    }
}