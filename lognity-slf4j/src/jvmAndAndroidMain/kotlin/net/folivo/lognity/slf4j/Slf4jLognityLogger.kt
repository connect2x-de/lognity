package net.folivo.lognity.slf4j

import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.config.config
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Context
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.logger.context
import net.folivo.lognity.api.marker.Marker
import org.slf4j.Logger as Slf4jLogger

internal class Slf4jLognityLogger(
    val delegate: Slf4jLogger
) : Logger {
    override val config: Config = config(Backend.configSpec)
    override val context: Context = context { }
    override var level: Level = Level.default
    override var isEnabled: Boolean = true

    private val formatPattern = config.appenders.first().pattern // Yank the first available log pattern for interop

    override fun log(level: Level, message: AnsiScope.() -> Any) = log(null, level, message)

    override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any) {
        if (level < this.level || marker?.isEnabled == false) return
        val messageContent = message(AnsiScope)
        val formattedMessage = Formatter.default(this, level, messageContent, marker, formatPattern)
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