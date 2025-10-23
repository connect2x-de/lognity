package net.folivo.lognity.ktor

import io.ktor.util.logging.*
import net.folivo.lognity.api.logger.Level

/**
 * Convert a Lognity [Level] to a Ktor [LogLevel].
 *
 * Mapping:
 * - TRACE -> TRACE
 * - DEBUG -> DEBUG
 * - INFO -> INFO
 * - WARN -> WARN
 * - ERROR and FATAL -> ERROR (Ktor has no distinct FATAL level)
 */
fun Level.asKtorLevel(): LogLevel = when (this) {
    Level.TRACE -> LogLevel.TRACE
    Level.DEBUG -> LogLevel.DEBUG
    Level.INFO -> LogLevel.INFO
    Level.WARN -> LogLevel.WARN
    Level.ERROR, Level.FATAL -> LogLevel.ERROR
}

/**
 * Convert a Ktor [LogLevel] to a Lognity [Level].
 *
 * By default, both [LogLevel.ERROR] and [LogLevel.NONE] map to [Level.ERROR].
 * If [errorAsFatal] is set to true, they map to [Level.FATAL] instead.
 *
 * @param errorAsFatal Whether Ktor's ERROR/NONE should be treated as FATAL in Lognity.
 */
fun LogLevel.asLognityLevel(errorAsFatal: Boolean = false): Level = when (this) {
    LogLevel.TRACE -> Level.TRACE
    LogLevel.DEBUG -> Level.DEBUG
    LogLevel.INFO -> Level.INFO
    LogLevel.WARN -> Level.WARN
    LogLevel.ERROR, LogLevel.NONE -> if (errorAsFatal) Level.FATAL else Level.ERROR
}