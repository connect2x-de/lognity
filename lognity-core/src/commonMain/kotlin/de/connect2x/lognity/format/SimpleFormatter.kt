package de.connect2x.lognity.format

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.ansi.AnsiSequence
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.getNativeThreadId
import de.connect2x.lognity.backend.getThreadName
import de.connect2x.lognity.format.SimpleFormatter.Companion.default
import de.connect2x.lognity.util.ThreadLocal
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A simple, fast log message formatter that compiles format strings once and reuses them.
 *
 * The formatter resolves placeholders (variables) in a format string against a provided
 * [FormatterContext]. Placeholders are defined by name and mapped to a [CompiledFormat.Segment]
 * implementation supplied via [variables].
 *
 * Usage notes:
 * - Format strings are compiled on first use and cached for subsequent calls.
 * - A lightweight thread-local [FormatterContext] is reused to minimize allocations.
 * - See [default] for a preconfigured instance with common variables like time, level, thread, etc.
 *
 * @param variables Mapping of variable names to [CompiledFormat.Segment]s used during compilation of
 *   format strings. The keys represent placeholder names that can be used in format strings and the
 *   values define how those placeholders are rendered from a [FormatterContext].
 */
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

        /**
         * A preconfigured [SimpleFormatter] that exposes a useful set of variables commonly needed in log output.
         *
         * Available variables in format strings:
         * - r: ANSI reset sequence
         * - levelColor: ANSI color sequence for the log [Level]
         * - marker: optional [Marker] name
         * - message: log message content
         * - thread: current thread name
         * - threadId: current thread id
         * - level: fixed-width level name padded to the longest level
         * - levelSymbol: short symbol representing the level
         * - name: [Logger] name
         * - yyyy, MM, dd, hh, mm, ss, SSS: current date-time components
         *
         * Notes:
         * - Date/time variables are evaluated at call time using [Clock.System.now].
         * - ANSI sequences depend on the chosen [Level] configuration.
         */
        val default: SimpleFormatter = SimpleFormatter(mapOf( // @formatter:off
            "r" to CompiledFormat.Text(AnsiSequence.reset.toString()),
            "levelColor" to CompiledFormat.Variable { ctx -> ctx.level.ansi.toString() },
            "marker" to CompiledFormat.Variable { ctx -> ctx.marker?.name ?: "" },
            "message" to CompiledFormat.Variable { ctx -> ctx.content.toString() },
            "thread" to CompiledFormat.Variable { getThreadName() },
            "threadId" to CompiledFormat.Variable { getNativeThreadId().toString() },
            "level" to CompiledFormat.Variable { ctx -> paddedLevelNames[ctx.level.ordinal] },
            "levelSymbol" to CompiledFormat.Variable { ctx -> ctx.level.symbol },
            "name" to CompiledFormat.Variable { ctx -> ctx.logger.context[Logger.Name]?.name ?: "" },
            "yyyy" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(yearFormat) },
            "MM" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(monthFormat) },
            "dd" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(dayFormat) },
            "hh" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(hourFormat) },
            "mm" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(minuteFormat) },
            "ss" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(secondFormat) },
            "SSS" to CompiledFormat.Variable { ctx -> ctx.timestamp.format(secondFractionFormat) }
        )) // @formatter:on
    }

    private val formats: SharedHashMap<String, CompiledFormat<FormatterContext>> = SharedHashMap()

    /**
     * Formats a log entry according to the provided format string [s].
     *
     * The format string may reference any variables known to this formatter (see [default] for
     * the built-in set). The format is compiled on first use and cached for subsequent calls.
     * This method is thread-safe and optimized to minimize allocations.
     *
     * @param logger The source [Logger].
     * @param level The [Level] of the log entry.
     * @param content The log message or object to be rendered.
     * @param marker Optional [Marker] for additional classification.
     * @param s The format string containing placeholders to resolve.
     * @return The formatted log line.
     */
    override operator fun invoke(
        logger: Logger, level: Level, content: Any, marker: Marker?, timestamp: Instant, s: String
    ): String {
        val format = formats.getOrPut(s) { CompiledFormat.compile(variables, s) }
        return format(context.get().apply {
            this.logger = logger
            this.level = level
            this.content = content
            this.marker = marker
            this.timestamp = timestamp
        })
    }
}