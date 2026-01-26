package de.connect2x.lognity.ktor

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import io.ktor.client.plugins.logging.Logger as KtorLogger

@PublishedApi
internal class LognityKtorClientLogger(
    private val delegate: Logger, private val targetLevel: Level
) : KtorLogger {
    override fun log(message: String) = when (targetLevel) {
        Level.TRACE -> delegate.trace { message }
        Level.DEBUG -> delegate.debug { message }
        Level.INFO -> delegate.info { message }
        Level.WARN -> delegate.warn { message }
        Level.ERROR -> delegate.error { message }
        Level.FATAL -> delegate.fatal { message }
    }
}

/**
 * Returns a Ktor [io.ktor.client.plugins.logging.Logger] that delegates to this Lognity [Logger].
 *
 * This is a lightweight adapter so you can plug Lognity into Ktor APIs expecting a Ktor logger.
 * The returned logger will forward log messages to the underlying Lognity logger implementation.
 *
 * @param targetLevel The level at which all messages for the given client should be logged.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Logger.asKtorClientLogger(
    targetLevel: Level = Level.INFO
): KtorLogger = LognityKtorClientLogger(this, targetLevel)