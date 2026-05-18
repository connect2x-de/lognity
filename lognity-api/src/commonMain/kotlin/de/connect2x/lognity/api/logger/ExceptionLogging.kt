package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.marker.Marker

/**
 * Log a TRACE level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [MessageScope]. The returned value is converted to `String`.
 */
inline fun Logger.trace( // @formatter:off
    throwable: Throwable?,
    marker: Marker? = null,
    crossinline message: MessageProvider
) { // @formatter:on
    trace(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log a DEBUG level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [MessageScope]. The returned value is converted to `String`.
 */
inline fun Logger.debug( // @formatter:off
    throwable: Throwable?,
    marker: Marker? = null,
    crossinline message: MessageProvider
) { // @formatter:on
    debug(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log an INFO level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [MessageScope]. The returned value is converted to `String`.
 */
inline fun Logger.info( // @formatter:off
    throwable: Throwable?,
    marker: Marker? = null,
    crossinline message: MessageProvider
) { // @formatter:on
    info(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log a WARN level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [MessageScope]. The returned value is converted to `String`.
 */
inline fun Logger.warn( // @formatter:off
    throwable: Throwable?,
    marker: Marker? = null,
    crossinline message: MessageProvider
) { // @formatter:on
    warn(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log an ERROR level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [MessageScope]. The returned value is converted to `String`.
 */
inline fun Logger.error( // @formatter:off
    throwable: Throwable?,
    marker: Marker? = null,
    crossinline message: MessageProvider
) {
    error(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}

/**
 * Log a FATAL level message with an optional [throwable] stack trace appended.
 *
 * @receiver Logger instance used to emit the log entry.
 * @param throwable The exception whose stack trace will be appended. If `null`, a placeholder text is used.
 * @param marker Optional [Marker] that can be used by backends/filters to tag the log entry.
 * @param message Message builder executed in an [MessageScope]. The returned value is converted to `String`.
 */
inline fun Logger.fatal( // @formatter:off
    throwable: Throwable?,
    marker: Marker? = null,
    crossinline message: MessageProvider
) { // @formatter:on
    fatal(marker) { "${message()}: ${throwable?.stackTraceToString() ?: "Stacktrace unavailable"}" }
}