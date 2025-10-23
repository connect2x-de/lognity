package net.folivo.lognity.ktor

import io.ktor.util.logging.*
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.logger.debug
import net.folivo.lognity.api.logger.error
import net.folivo.lognity.api.logger.info
import net.folivo.lognity.api.logger.trace
import net.folivo.lognity.api.logger.warn
import io.ktor.util.logging.Logger as KtorLogger

@PublishedApi
internal actual class LognityKtorLogger actual constructor(
    private val delegate: Logger
) : KtorLogger {
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

    override val level: LogLevel
        get() = delegate.level.asKtorLevel()
}