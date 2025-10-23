package net.folivo.lognity.java

import net.folivo.lognity.api.logger.Level
import java.util.logging.Level as JavaLevel

/**
 * Convert a Lognity [Level] to the corresponding [java.util.logging.Level].
 *
 * Mapping:
 * - TRACE -> [JavaLevel.FINEST]
 * - DEBUG -> [JavaLevel.FINE]
 * - INFO  -> [JavaLevel.INFO]
 * - WARN  -> [JavaLevel.WARNING]
 * - ERROR, FATAL -> [JavaLevel.SEVERE]
 */
fun Level.asJavaLevel(): JavaLevel = when (this) {
    Level.TRACE -> JavaLevel.FINEST
    Level.DEBUG -> JavaLevel.FINE
    Level.INFO -> JavaLevel.INFO
    Level.WARN -> JavaLevel.WARNING
    Level.ERROR, Level.FATAL -> JavaLevel.SEVERE
}

/**
 * Convert a [java.util.logging.Level] to the closest matching Lognity [Level].
 *
 * Notes:
 * - JUL has no distinct FATAL level. We consider [JavaLevel.OFF] as FATAL, since it indicates that logging is disabled.
 * - [JavaLevel.SEVERE] maps to ERROR.
 * - Less granular JUL levels are grouped to the nearest Lognity level.
 *
 * Mapping:
 * - ALL, FINEST, FINER -> TRACE
 * - FINE -> DEBUG
 * - WARNING -> WARN
 * - SEVERE -> ERROR
 * - OFF -> FATAL
 * - others (e.g., CONFIG, INFO) -> INFO
 */
fun JavaLevel.asLognityLevel(): Level = when (this) {
    JavaLevel.ALL, JavaLevel.FINEST, JavaLevel.FINER -> Level.TRACE
    JavaLevel.FINE -> Level.DEBUG
    JavaLevel.WARNING -> Level.WARN
    JavaLevel.SEVERE -> Level.ERROR
    JavaLevel.OFF -> Level.FATAL
    else -> Level.INFO
}