package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi

@ExperimentalLoggingApi
enum class LogstashDropReason { NOT_STARTED, QUEUE_FULL, SHUTDOWN }

/** Listener exceptions are isolated from logging. Callbacks can run on producers or the writer. */
@ExperimentalLoggingApi
interface LogstashListener {
    fun appenderStarted() = Unit
    fun appenderStopped() = Unit
    fun connectionOpened(destination: LogstashDestination) = Unit
    fun connectionClosed(destination: LogstashDestination) = Unit
    fun connectionFailed(destination: LogstashDestination, cause: Throwable) = Unit
    fun eventAppended(event: LogstashEvent) = Unit
    fun eventSent(event: LogstashEvent, byteCount: Int) = Unit
    fun eventDropped(event: LogstashEvent, reason: LogstashDropReason) = Unit
    fun eventFailed(event: LogstashEvent, cause: Throwable) = Unit
    fun writerFailed(cause: Throwable) = Unit
}