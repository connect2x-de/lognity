package de.connect2x.lognity.ktor

import de.connect2x.lognity.api.logger.Logger
import io.ktor.util.logging.Logger as KtorLogger

@PublishedApi
internal expect class LognityKtorLogger(delegate: Logger) : KtorLogger {
    override fun debug(message: String)
    override fun debug(message: String, cause: Throwable)
    override fun error(message: String)
    override fun error(message: String, cause: Throwable)
    override fun info(message: String)
    override fun info(message: String, cause: Throwable)
    override fun trace(message: String)
    override fun trace(message: String, cause: Throwable)
    override fun warn(message: String)
    override fun warn(message: String, cause: Throwable)
}

/**
 * Returns a Ktor [io.ktor.util.logging.Logger] that delegates to this Lognity [Logger].
 *
 * This is a lightweight adapter so you can plug Lognity into Ktor APIs expecting a Ktor logger.
 * The returned logger will forward log messages to the underlying Lognity logger implementation.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Logger.asKtorLogger(): KtorLogger = LognityKtorLogger(this)