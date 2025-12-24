package de.connect2x.lognity.util

import de.connect2x.lognity.api.logger.Level
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_FAULT
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.os_log_type_t

/**
 * Apple os_log type corresponding to this [Level].
 *
 * This maps Lognity levels to Darwin's unified logging system types used by os_log.
 *
 * Mapping:
 * - TRACE -> [platform.darwin.OS_LOG_TYPE_DEBUG]
 * - DEBUG -> [platform.darwin.OS_LOG_TYPE_DEBUG]
 * - INFO -> [platform.darwin.OS_LOG_TYPE_INFO]
 * - WARN -> [platform.darwin.OS_LOG_TYPE_INFO]
 * - ERROR -> [platform.darwin.OS_LOG_TYPE_ERROR]
 * - FATAL -> [platform.darwin.OS_LOG_TYPE_FAULT]
 */
val Level.osLogType: os_log_type_t
    get() = when (this) {
        Level.DEBUG, Level.TRACE -> OS_LOG_TYPE_DEBUG
        Level.INFO, Level.WARN -> OS_LOG_TYPE_INFO
        Level.ERROR -> OS_LOG_TYPE_ERROR
        Level.FATAL -> OS_LOG_TYPE_FAULT
    }