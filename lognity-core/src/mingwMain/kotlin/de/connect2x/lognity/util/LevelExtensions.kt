package de.connect2x.lognity.util

import de.connect2x.lognity.api.logger.Level
import platform.windows.EVENTLOG_ERROR_TYPE
import platform.windows.EVENTLOG_INFORMATION_TYPE
import platform.windows.EVENTLOG_WARNING_TYPE
import platform.windows.WORD

/**
 * Windows Event Log type corresponding to this [Level].
 *
 * This maps Lognity levels to Win32 Event Log types used by ReportEvent.
 *
 * Mapping:
 * - TRACE -> [EVENTLOG_INFORMATION_TYPE]
 * - DEBUG -> [EVENTLOG_INFORMATION_TYPE]
 * - INFO -> [EVENTLOG_INFORMATION_TYPE]
 * - WARN -> [EVENTLOG_WARNING_TYPE]
 * - ERROR -> [EVENTLOG_ERROR_TYPE]
 * - FATAL -> [EVENTLOG_ERROR_TYPE]
 */
val Level.eventType: WORD
    get() = when (this) {
        Level.TRACE, Level.DEBUG, Level.INFO -> EVENTLOG_INFORMATION_TYPE
        Level.WARN -> EVENTLOG_WARNING_TYPE
        Level.ERROR, Level.FATAL -> EVENTLOG_ERROR_TYPE
    }.toUShort()