package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedLinkedList
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.joinBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalAtomicApi::class, ExperimentalTime::class)
class MessageAggregator( // @formatter:off
    val coroutineScope: CoroutineScope,
    val messageCallback: suspend (Message) -> Unit,
    val closeCallback: suspend () -> Unit = {},
) : AutoCloseable { // @formatter:on
    data class Message( // @formatter:off
        val logger: Logger,
        val level: Level,
        val message: String,
        val marker: Marker?
    ) // @formatter:on

    private val queue: SharedLinkedList<Message> = SharedLinkedList()
    private val isFlushJobRunning: AtomicBoolean = AtomicBoolean(true)

    private val flushJob: Job = coroutineScope.launch {
        try {
            while (true) {
                while (queue.isNotEmpty()) messageCallback(queue.removeFirst())
                if (!isFlushJobRunning.load()) break
                delay(25.milliseconds)
            }
        }
        finally {
            withContext(NonCancellable) {
                while (queue.isNotEmpty()) messageCallback(queue.removeFirst())
                closeCallback()
            }
        }
    }

    fun enqueue(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!isFlushJobRunning.load()) error("Message aggregator is already shut down :(")
        queue += Message(logger, level, message, marker)
    }

    override fun close() {
        check(isFlushJobRunning.compareAndExchange(expectedValue = true, newValue = false)) {
            "Message aggregator was already closed"
        }
        flushJob.joinBlocking()
    }
}