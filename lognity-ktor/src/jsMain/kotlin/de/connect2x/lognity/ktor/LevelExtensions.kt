package de.connect2x.lognity.ktor

import de.connect2x.lognity.api.logger.Level
import io.ktor.util.logging.*

/**
 * Convert a Lognity [Level] to a Ktor [io.ktor.util.logging.LogLevel].
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