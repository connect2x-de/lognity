package net.folivo.lognity.api.logger

import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.marker.Marker

/**
 * Log a TRACE level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [AnsiScope]. The returned value is converted to `String`.
 */
inline fun Logger.trace(throwable: Throwable?, marker: Marker? = null, crossinline message: AnsiScope.() -> Any) {
    trace(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log a DEBUG level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [AnsiScope]. The returned value is converted to `String`.
 */
inline fun Logger.debug(throwable: Throwable?, marker: Marker? = null, crossinline message: AnsiScope.() -> Any) {
    debug(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log an INFO level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [AnsiScope]. The returned value is converted to `String`.
 */
inline fun Logger.info(throwable: Throwable?, marker: Marker? = null, crossinline message: AnsiScope.() -> Any) {
    info(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log a WARN level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [AnsiScope]. The returned value is converted to `String`.
 */
inline fun Logger.warn(throwable: Throwable?, marker: Marker? = null, crossinline message: AnsiScope.() -> Any) {
    warn(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log an ERROR level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [AnsiScope]. The returned value is converted to `String`.
 */
inline fun Logger.error(throwable: Throwable?, marker: Marker? = null, crossinline message: AnsiScope.() -> Any) {
    error(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log a FATAL level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [AnsiScope]. The returned value is converted to `String`.
 */
inline fun Logger.fatal(throwable: Throwable?, marker: Marker? = null, crossinline message: AnsiScope.() -> Any) {
    fatal(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}