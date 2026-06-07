package de.connect2x.lognity.appender

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.format.CompiledFormat
import de.connect2x.lognity.format.FormatterContext
import de.connect2x.lognity.io.ioDispatcher
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicReference

abstract class AbstractAppender : Appender {
    internal val cachedFormat: AtomicReference<CompiledFormat<FormatterContext>?> = AtomicReference(null)

    @ExperimentalLoggingApi
    override suspend fun appendSuspend(logger: Logger, level: Level, message: String, marker: Marker?) {
        withContext(ioDispatcher) {
            append(logger, level, message, marker)
        }
    }

    @ExperimentalLoggingApi
    override suspend fun flushSuspend() {
        withContext(ioDispatcher) {
            flush()
        }
    }
}