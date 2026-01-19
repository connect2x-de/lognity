package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.backend.ShutdownHandler
import de.connect2x.lognity.io.MessageAggregator

abstract class AbstractAggregatingAppender : Appender {
    protected val aggregator: MessageAggregator = MessageAggregator( // @formatter:off
        coroutineScope = DefaultBackend.coroutineScope,
        messageCallback = ::writeToOutput,
        closeCallback = ::afterAggregatorShutdown
    ) // @formatter:on

    init {
        ShutdownHandler.register(::dispose, priority = 99) // After everything else but before backend itself
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (level < logger.level || message.isEmpty() || !filter(level, message, marker)) return
        aggregator.enqueue(logger, level, message, marker)
    }

    protected abstract suspend fun writeToOutput(message: MessageAggregator.Message)

    protected open suspend fun afterAggregatorShutdown() = flush()

    protected open fun dispose() {
        aggregator.close()
    }
}