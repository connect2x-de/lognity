package de.connect2x.lognity.ktor

import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.debug
import de.connect2x.lognity.api.logger.error
import de.connect2x.lognity.api.logger.info
import de.connect2x.lognity.api.logger.trace
import de.connect2x.lognity.api.logger.warn
import io.ktor.util.logging.*
import io.ktor.util.logging.Logger as KtorLogger

@PublishedApi
internal actual class LognityKtorServerLogger actual constructor(
    private val delegate: Logger
) : KtorLogger {
    override val level: LogLevel
        get() = delegate.level.asKtorLevel()

    actual override fun debug(message: String) = delegate.debug { message }
    actual override fun debug(message: String, cause: Throwable) = delegate.debug(cause) { message }
    actual override fun error(message: String) = delegate.error { message }
    actual override fun error(message: String, cause: Throwable) = delegate.error(cause) { message }
    actual override fun info(message: String) = delegate.info { message }
    actual override fun info(message: String, cause: Throwable) = delegate.info(cause) { message }
    actual override fun trace(message: String) = delegate.trace { message }
    actual override fun trace(message: String, cause: Throwable) = delegate.trace(cause) { message }
    actual override fun warn(message: String) = delegate.warn { message }
    actual override fun warn(message: String, cause: Throwable) = delegate.warn(cause) { message }
}