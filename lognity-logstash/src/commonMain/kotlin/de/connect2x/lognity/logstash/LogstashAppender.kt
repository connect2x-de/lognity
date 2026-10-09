package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicLong
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * Asynchronous TCP appender for Logstash, using a single coroutine writer and a bounded queue.
 * Messages received from Lognity are already formatted; [encoder] supplies the wire representation.
 *
 * Construction starts the writer unless [autoStart] is false. Call [stop] or [stopSuspend] on shutdown
 * to drain accepted events within the configured grace period. Stopped instances cannot be restarted.
 * [flush] is a FIFO barrier for previously accepted events, not an acknowledgement from Logstash.
 * TCP cannot guarantee delivery: reconnection can lose or duplicate events after a partial write.
 */
class LogstashAppender(
    config: LogstashConfig,
    val encoder: LogstashEncoder = LogstashJsonEncoder(),
    override val pattern: String = "{{message}}",
    override val formatter: Formatter = Formatter.identity,
    override val filter: Filter = Filter.always,
    override val name: String? = null,
    autoStart: Boolean = true
) : Appender, AutoCloseable {
    val config: LogstashConfig = config.copy(
        destinations = config.destinations.toList(),
        listeners = config.listeners.toList(),
        keepAliveBytes = config.keepAliveBytes?.copyOf()
    )
    private val started = AtomicBoolean(false)
    private val stopped = AtomicBoolean(false)
    private val dropped = AtomicLong(0)
    private val scope = CoroutineScope(SupervisorJob() + this.config.dispatcher)
    private val queue = Channel<LogstashCommand>(this.config.ringBufferSize, onUndeliveredElement = ::undelivered)
    private val worker = scope.launch(start = CoroutineStart.LAZY) {
        try {
            if (started.load()) {
                notify { appenderStarted() }
                LogstashWriter(this@LogstashAppender, queue).run()
            }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            notify { writerFailed(cause) }
        }
    }.also { job ->
        job.invokeOnCompletion {
            stopped.store(true)
            queue.cancel()
            notify { appenderStopped() }
            scope.cancel()
        }
    }

    val isStarted: Boolean get() = started.load() && !stopped.load()
    val droppedEventCount: Long get() = dropped.load()

    init {
        if (autoStart) start()
    }

    fun start() {
        check(!stopped.load()) { "A stopped LogstashAppender cannot be restarted" }
        if (started.compareAndSet(false, true)) worker.start()
    }

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        val command = event(logger, level, message, marker)
        if (!tryAppend(command)) runBlocking { enqueue(command) }
    }

    @ExperimentalLoggingApi
    override suspend fun appendSuspend(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(logger, level, message, marker)) return
        val command = event(logger, level, message, marker)
        if (!tryAppend(command)) enqueue(command)
    }

    @OptIn(ExperimentalLoggingApi::class)
    override fun flush() = runBlocking { flushSuspend() }

    @ExperimentalLoggingApi
    override suspend fun flushSuspend() {
        if (!started.load()) return
        withTimeout(config.flushTimeout) {
            if (stopped.load()) {
                worker.join()
                return@withTimeout
            }
            val completion = CompletableDeferred<Unit>()
            try {
                queue.send(LogstashCommand.Flush(completion))
            } catch (_: ClosedSendChannelException) {
                worker.join()
                return@withTimeout
            }
            completion.await()
        }
    }

    fun stop() = runBlocking { stopSuspend() }

    /** Cancellation of the caller still closes the writer and all its resources. */
    suspend fun stopSuspend() {
        stopped.store(true)
        queue.close()
        worker.start()
        try {
            if (withTimeoutOrNull(config.shutdownGracePeriod) { worker.join(); true } == null) worker.cancel()
        }
        finally {
            withContext(NonCancellable) { worker.cancelAndJoin() }
        }
    }

    override fun close() = stop()

    private fun event(logger: Logger, level: Level, message: String, marker: Marker?) = LogstashCommand.Event(
        LogstashEvent(Clock.System.now(), logger, logger.context[Logger.Name]?.name, level, message, marker?.name)
    )

    private fun tryAppend(command: LogstashCommand.Event): Boolean {
        if (!isStarted) {
            drop(command, LogstashDropReason.NOT_STARTED)
            return true
        }
        val result = queue.trySend(command)
        if (result.isSuccess) {
            notify { eventAppended(command.event) }
            return true
        }
        if (result.isClosed || config.appendTimeout == Duration.ZERO) {
            drop(command, if (result.isClosed) LogstashDropReason.SHUTDOWN else LogstashDropReason.QUEUE_FULL)
            return true
        }
        return false
    }

    private suspend fun enqueue(command: LogstashCommand.Event) {
        try {
            val sent = if (config.appendTimeout < Duration.ZERO || config.appendTimeout == Duration.INFINITE) {
                queue.send(command)
                true
            }
            else withTimeoutOrNull(config.appendTimeout) { queue.send(command); true } ?: false
            if (sent) notify { eventAppended(command.event) }
            else drop(command, LogstashDropReason.QUEUE_FULL)
        } catch (_: ClosedSendChannelException) {
            drop(command, LogstashDropReason.SHUTDOWN)
        }
    }

    internal fun notify(block: LogstashListener.() -> Unit) {
        config.listeners.forEach { listener ->
            try {
                listener.block()
            } catch (_: Throwable) {
                // Listener failures must not take down a logging producer or the socket writer.
            }
        }
    }

    internal fun drop(command: LogstashCommand.Event, reason: LogstashDropReason) {
        if (command.completed.compareAndSet(false, true)) {
            dropped.fetchAndAdd(1)
            notify { eventDropped(command.event, reason) }
        }
    }

    private fun undelivered(command: LogstashCommand) {
        when (command) {
            is LogstashCommand.Event -> drop(
                command, if (stopped.load()) LogstashDropReason.SHUTDOWN else LogstashDropReason.QUEUE_FULL
            )

            is LogstashCommand.Flush -> command.completion.completeExceptionally(IllegalStateException("Appender stopped before flushing"))
        }
    }
}

internal sealed interface LogstashCommand {
    class Event(val event: LogstashEvent) : LogstashCommand {
        val completed = AtomicBoolean(false)
    }

    class Flush(val completion: CompletableDeferred<Unit>) : LogstashCommand
}