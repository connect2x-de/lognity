package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        runBlocking {
            ioMutex.withLock {
                super.append(logger, level, message, marker)
            }
        }
    }
}