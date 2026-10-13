package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.marker.Marker

internal class TestLogger : Logger {
    override val config = Config {}
    override val context = Context { value(Logger.Name("test.logger")) }
    override var level = Level.TRACE
    override var isEnabled = true
    override fun log(level: Level, message: MessageProvider) = Unit
    override fun log(marker: Marker?, level: Level, message: MessageProvider) = Unit
}

@OptIn(ExperimentalLoggingApi::class)
internal class RecordingSocket(
    private val writeAction: suspend (ByteArray) -> Unit = {}
) : LogstashSocket {
    override var isClosed = false
    val writes = mutableListOf<String>()
    var flushes = 0
    override suspend fun write(bytes: ByteArray) {
        check(!isClosed)
        writeAction(bytes)
        writes += bytes.decodeToString()
    }

    override suspend fun flush() {
        flushes++
    }

    override fun close() {
        isClosed = true
    }
}