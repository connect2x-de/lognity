package de.connect2x.lognity.format

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.ansi.AnsiSequence
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.appender.AbstractAppender
import de.connect2x.lognity.format.CompiledFormat.Segment
import de.connect2x.lognity.format.SimpleFormatter.Companion.default
import de.connect2x.lognity.util.ThreadLocal
import de.connect2x.lognity.util.getThreadId
import de.connect2x.lognity.util.getThreadName
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlin.time.Clock

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
    internal val variables: Map<String, Segment<FormatterContext>>
) : Formatter {
    companion object {
        private const val NA_PLACEHOLDER: String = "(_)"

        private val context: ThreadLocal<FormatterContext> = ThreadLocal { FormatterContext() }

        private val maxLevelNameLength: Int = Level.entries //
            .maxOf { it.name.length }

        private val paddedLevelNames: Array<String> = Level.entries //
            .map { it.name.padEnd(maxLevelNameLength, '-') } //
            .toTypedArray()

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
        val default: SimpleFormatter = SimpleFormatter(HashMap<String, Segment<FormatterContext>>().apply {
            this += "r" to CompiledFormat.Text(AnsiSequence.reset.toString())
            this += "marker" to CompiledFormat.Variable(::formatMarker)
            this += "message" to CompiledFormat.Variable(::formatMessage)
            this += "thread" to CompiledFormat.Variable(::formatThreadName)
            this += "threadId" to CompiledFormat.Variable(::formatThreadId)
            this += "levelColor" to CompiledFormat.Variable(::formatLevelColor)
            this += "level" to CompiledFormat.Variable(::formatLevelName)
            this += "levelSymbol" to CompiledFormat.Variable(::formatLevelSymbol)
            this += "name" to CompiledFormat.Variable(::formatLoggerName)
            this += "coroutineName" to CompiledFormat.Variable(::formatCoroutineName)
            this += "yyyy" to CompiledFormat.Variable(::formatYear)
            this += "MM" to CompiledFormat.Variable(::formatMonth)
            this += "dd" to CompiledFormat.Variable(::formatDay)
            this += "hh" to CompiledFormat.Variable(::formatHour)
            this += "mm" to CompiledFormat.Variable(::formatMinute)
            this += "ss" to CompiledFormat.Variable(::formatSecond)
            this += "SSS" to CompiledFormat.Variable(::formatSecondFraction)
        })

        private fun formatYear(context: FormatterContext): String = context.timestamp.year.toString()
        private fun formatMonth(context: FormatterContext): String =
            context.timestamp.month.number.toString().padStart(2, '0')

        private fun formatDay(context: FormatterContext): String = context.timestamp.day.toString().padStart(2, '0')
        private fun formatHour(context: FormatterContext): String = context.timestamp.hour.toString().padStart(2, '0')
        private fun formatMinute(context: FormatterContext): String =
            context.timestamp.minute.toString().padStart(2, '0')

        private fun formatSecond(context: FormatterContext): String =
            context.timestamp.second.toString().padStart(2, '0')

        private fun formatSecondFraction(context: FormatterContext): String {
            val fraction = (context.timestamp.nanosecond % 1000000000).toDouble() / 1000000000.0
            val result = fraction.toString().substringAfter('.')
            return when {
                result.length > 3 -> result.take(3)
                result.length < 3 -> result.padStart(3, '0')
                else -> result
            }
        }

        private fun formatLevelColor(context: FormatterContext): String {
            val config = context.logger.config
            return config.levelColors[context.level].toString()
        }

        private fun formatLevelName(context: FormatterContext): String = paddedLevelNames[context.level.ordinal]
        private fun formatLevelSymbol(context: FormatterContext): String = context.level.symbol

        private fun formatMarker(context: FormatterContext): String = context.marker?.name ?: NA_PLACEHOLDER
        private fun formatMessage(context: FormatterContext): String = context.content.toString()

        @Suppress("UNUSED_PARAMETER")
        private fun formatThreadName(context: FormatterContext): String = getThreadName()

        @Suppress("UNUSED_PARAMETER")
        private fun formatThreadId(context: FormatterContext): String = getThreadId().toString()

        private fun formatLoggerName(context: FormatterContext): String {
            return context.logger.context[Logger.Name]?.name ?: NA_PLACEHOLDER
        }

        private fun formatCoroutineName(context: FormatterContext): String {
            return context.logger.context[Logger.CoroutineName]?.name ?: NA_PLACEHOLDER
        }
    }

    private val cachedFormatters: SharedHashMap<String, CompiledFormat<FormatterContext>> = SharedHashMap()

    private fun getCompiledFormat(appender: Appender): CompiledFormat<FormatterContext> = when (appender) {
        is AbstractAppender -> {
            var format = appender.cachedFormat.load()
            if (format == null) {
                format = CompiledFormat.compile(variables, appender.pattern)
                appender.cachedFormat.store(format)
            }
            format
        }

        else -> {
            val pattern = appender.pattern
            cachedFormatters.getOrPut(pattern) {
                CompiledFormat.compile(variables, pattern)
            }
        }
    }

    override operator fun invoke( // @formatter:off
        logger: Logger,
        appender: Appender,
        level: Level,
        content: Any,
        marker: Marker?,
        timestamp: LocalDateTime,
    ): String { // @formatter:on
        return getCompiledFormat(appender)(
            context.get().apply {
                this.logger = logger
                this.level = level
                this.content = content
                this.marker = marker
                this.timestamp = timestamp
            },
        )
    }
}
