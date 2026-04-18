package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.Mutex
import de.connect2x.lognity.util.withLock
import de.connect2x.lognity.util.withLockSuspend

private val ioMutex: Mutex = Mutex()

/**
 * Same as [ConsoleAppender], except that all writes all guarded by a [Mutex]
 * to prevent mangled lines when printing from many coroutines.
 */
class SynchronizedConsoleAppender( // @formatter:off
    pattern: String,
    formatter: Formatter,
    filter: Filter,
    name: String? = null
) : ConsoleAppender(pattern, formatter, filter, name) { // @formatter:on
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) = ioMutex.withLock {
        super.append(logger, level, message, marker)
    }

    @ExperimentalLoggingApi
    override suspend fun appendSuspend(logger: Logger, level: Level, message: String, marker: Marker?) =
        ioMutex.withLockSuspend {
            super.append(logger, level, message, marker)
        }
}