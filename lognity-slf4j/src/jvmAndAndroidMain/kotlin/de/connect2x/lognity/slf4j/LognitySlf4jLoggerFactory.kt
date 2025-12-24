package de.connect2x.lognity.slf4j

import de.connect2x.lognity.api.logger.Logger
import org.slf4j.ILoggerFactory
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.Logger as Slf4jLogger

/**
 * SLF4J [ILoggerFactory] implementation backed by Lognity.
 *
 * This factory is discovered and used by SLF4J to create loggers. It will return
 * one [org.slf4j.Logger] per logger name and internally delegate all calls to the
 * corresponding Lognity [de.connect2x.lognity.api.logger.Logger].
 */
object LognitySlf4jLoggerFactory : ILoggerFactory {
    private val loggers: ConcurrentHashMap<String, LognitySlf4jLogger> = ConcurrentHashMap()

    /**
     * Returns an SLF4J logger for the given [name].
     *
     * The returned logger is cached per-name and bridges to a Lognity logger instance.
     */
    override fun getLogger(name: String): Slf4jLogger {
        return loggers.getOrPut(name) {
            LognitySlf4jLogger(Logger(name))
        }
    }
}