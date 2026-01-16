package de.connect2x.lognity.appender

import co.touchlab.stately.collections.SharedLinkedList
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.util.joinBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

/**
 * A simple message aggregator to allow non-blocking logging preventing lock contention
 * around log calls.
 * A new job is created using the given [CoroutineScope] which merges all per-thread queues
 * into a single coherent output.
 */
@OptIn(ExperimentalAtomicApi::class, ExperimentalTime::class)
class MessageAggregator( // @formatter:off
    val coroutineScope: CoroutineScope,
    val messageCallback: suspend (Message) -> Unit,
    val closeCallback: () -> Unit = {},
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
        // Make sure we always empty out all buffers before unblocking
        while (isFlushJobRunning.load() || queue.isNotEmpty()) {
            while (queue.isNotEmpty()) {
                messageCallback(queue.removeFirst())
                yield()
            }
            delay(10.milliseconds)
        }
        closeCallback()
    }

    fun enqueue(logger: Logger, level: Level, message: String, marker: Marker?) {
        queue += Message(logger, level, message, marker)
    }

    override fun close() {
        isFlushJobRunning.store(false)
        flushJob.joinBlocking()
    }
}