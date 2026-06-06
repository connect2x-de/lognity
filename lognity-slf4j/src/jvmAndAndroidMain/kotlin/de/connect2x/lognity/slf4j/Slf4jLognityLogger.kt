package de.connect2x.lognity.slf4j

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.EmptyContext
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.logger.invoke
import de.connect2x.lognity.api.marker.Marker
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.slf4j.Logger as Slf4jLogger

internal class Slf4jLognityLogger(
    val delegate: Slf4jLogger
) : Logger {
    override val config: Config = Config(Backend.configSpec)
    override val context: Context = EmptyContext
    override var level: Level = Level.default
    override var isEnabled: Boolean = true

    private val formatPattern = config.appenders.first().pattern // Yank the first available log pattern for interop

    override fun log(level: Level, message: MessageProvider) = log(null, level, message)

    override fun log(marker: Marker?, level: Level, message: MessageProvider) {
        if (level < this.level || marker?.isEnabled == false) return
        val messageContent = message(this) ?: "null"
        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val formattedMessage = Formatter.default(this, level, messageContent, marker, timestamp, formatPattern)
        when (level) {
            Level.TRACE -> delegate.trace(formattedMessage)
            Level.DEBUG -> delegate.debug(formattedMessage)
            Level.INFO -> delegate.info(formattedMessage)
            Level.WARN -> delegate.warn(formattedMessage)
            Level.ERROR, Level.FATAL -> delegate.error(formattedMessage)
        }
    }
}

/**
 * Converts an SLF4J [org.slf4j.Logger] to a Lognity [Logger].
 *
 * If the given logger is already a bridged instance, the underlying Lognity logger is returned.
 */
fun Slf4jLogger.asLognityLogger(): Logger = when (this) {
    is LognitySlf4jLogger -> delegate
    else -> Slf4jLognityLogger(this)
}