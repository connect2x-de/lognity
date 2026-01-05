package de.connect2x.lognity.util

import android.util.Log
import de.connect2x.lognity.api.logger.Level

/**
 * Android Logcat priority corresponding to this [Level].
 *
 * Mapping:
 * - TRACE -> [Log.VERBOSE]
 * - DEBUG -> [Log.DEBUG]
 * - INFO -> [Log.INFO]
 * - WARN -> [Log.WARN]
 * - ERROR -> [Log.ERROR]
 * - FATAL -> [Log.ASSERT]
 */
val Level.logcatLevel: Int
    get() = when (this) {
        Level.TRACE -> Log.VERBOSE
        Level.DEBUG -> Log.DEBUG
        Level.INFO -> Log.INFO
        Level.WARN -> Log.WARN
        Level.ERROR -> Log.ERROR
        Level.FATAL -> Log.ASSERT
    }