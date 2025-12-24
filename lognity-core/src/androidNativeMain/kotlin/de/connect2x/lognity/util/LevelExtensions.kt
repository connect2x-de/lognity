package de.connect2x.lognity.util

import de.connect2x.lognity.api.logger.Level
import platform.android.ANDROID_LOG_DEBUG
import platform.android.ANDROID_LOG_ERROR
import platform.android.ANDROID_LOG_FATAL
import platform.android.ANDROID_LOG_INFO
import platform.android.ANDROID_LOG_VERBOSE
import platform.android.ANDROID_LOG_WARN

/**
 * Android logcat priority corresponding to this [Level].
 *
 * Mapping:
 * - TRACE -> [ANDROID_LOG_VERBOSE]
 * - DEBUG -> [ANDROID_LOG_DEBUG]
 * - INFO -> [ANDROID_LOG_INFO]
 * - WARN -> [ANDROID_LOG_WARN]
 * - ERROR -> [ANDROID_LOG_ERROR]
 * - FATAL -> [ANDROID_LOG_FATAL]
 */
val Level.logcatLevel: Int
    get() = when (this) {
        Level.TRACE -> ANDROID_LOG_VERBOSE
        Level.DEBUG -> ANDROID_LOG_DEBUG
        Level.WARN -> ANDROID_LOG_WARN
        Level.ERROR -> ANDROID_LOG_ERROR
        Level.FATAL -> ANDROID_LOG_FATAL
        Level.INFO -> ANDROID_LOG_INFO
    }.toInt()