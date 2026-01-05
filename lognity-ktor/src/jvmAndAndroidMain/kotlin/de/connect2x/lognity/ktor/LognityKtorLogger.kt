package de.connect2x.lognity.ktor

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.slf4j.asLognityMarker
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import io.ktor.util.logging.Logger as KtorLogger

/**
 * We cannot use the SLF4j implementation directly as a type alias here,
 * because Ktor bodges the nullability and names of the original SLF4j API.
 */
@PublishedApi
internal actual class LognityKtorLogger actual constructor(
    private val delegate: Logger
) : KtorLogger {
    override fun getName(): String = delegate.context[Logger.Name]?.name ?: delegate.toString()

    override fun isTraceEnabled(): Boolean = delegate.level == Level.TRACE
    override fun isTraceEnabled(marker: Marker?): Boolean = isTraceEnabled
    actual override fun trace(message: String) = delegate.trace { message }

    override fun trace(format: String, arg: Any?) = delegate.trace {
        MessageFormatter.format(format, arg).message
    }

    override fun trace(format: String, arg1: Any?, arg2: Any?) = delegate.trace {
        MessageFormatter.format(format, arg1, arg2).message
    }

    override fun trace(format: String?, vararg arguments: Any?) = delegate.trace {
        MessageFormatter.arrayFormat(format, arguments).message
    }

    actual override fun trace(message: String, cause: Throwable) = delegate.trace {
        MessageFormatter.format(message, cause).message
    }

    override fun trace(marker: Marker?, message: String) = delegate.trace(marker?.asLognityMarker()) { message }

    override fun trace(marker: Marker?, format: String, arg: Any?) = delegate.trace(marker?.asLognityMarker()) {
        MessageFormatter.format(format, arg).message
    }

    override fun trace(marker: Marker?, format: String, arg1: Any?, arg2: Any?) =
        delegate.trace(marker?.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2).message
        }

    override fun trace(marker: Marker?, format: String, vararg argArray: Any?) =
        delegate.trace(marker?.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray).message
        }

    override fun trace(marker: Marker?, message: String, cause: Throwable?) =
        delegate.trace(marker?.asLognityMarker()) {
            MessageFormatter.format(message, cause).message
        }

    override fun isDebugEnabled(): Boolean = delegate.level <= Level.DEBUG
    override fun isDebugEnabled(marker: Marker?): Boolean = isDebugEnabled
    actual override fun debug(message: String) = delegate.debug { message }

    override fun debug(format: String, arg: Any?) = delegate.debug {
        MessageFormatter.format(format, arg).message
    }

    override fun debug(format: String, arg1: Any?, arg2: Any?) = delegate.debug {
        MessageFormatter.format(format, arg1, arg2).message
    }

    override fun debug(format: String?, vararg arguments: Any?) = delegate.debug {
        MessageFormatter.arrayFormat(format, arguments).message
    }

    actual override fun debug(message: String, cause: Throwable) = delegate.debug {
        MessageFormatter.format(message, cause).message
    }

    override fun debug(marker: Marker?, message: String) = delegate.debug(marker?.asLognityMarker()) { message }

    override fun debug(marker: Marker?, format: String, arg: Any?) = delegate.debug(marker?.asLognityMarker()) {
        MessageFormatter.format(format, arg).message
    }

    override fun debug(marker: Marker?, format: String, arg1: Any?, arg2: Any?) =
        delegate.debug(marker?.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2).message
        }

    override fun debug(marker: Marker?, format: String, vararg argArray: Any?) =
        delegate.debug(marker?.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray).message
        }

    override fun debug(marker: Marker?, message: String, cause: Throwable?) =
        delegate.debug(marker?.asLognityMarker()) {
            MessageFormatter.format(message, cause).message
        }

    override fun isInfoEnabled(): Boolean = delegate.level <= Level.INFO
    override fun isInfoEnabled(marker: Marker?): Boolean = isInfoEnabled
    actual override fun info(message: String) = delegate.info { message }

    override fun info(format: String, arg: Any?) = delegate.info {
        MessageFormatter.format(format, arg).message
    }

    override fun info(format: String, arg1: Any?, arg2: Any?) = delegate.info {
        MessageFormatter.format(format, arg1, arg2).message
    }

    override fun info(format: String?, vararg arguments: Any?) = delegate.info {
        MessageFormatter.arrayFormat(format, arguments).message
    }

    actual override fun info(message: String, cause: Throwable) = delegate.info {
        MessageFormatter.format(message, cause).message
    }

    override fun info(marker: Marker?, message: String) = delegate.info(marker?.asLognityMarker()) { message }

    override fun info(marker: Marker?, format: String, arg: Any?) = delegate.info(marker?.asLognityMarker()) {
        MessageFormatter.format(format, arg).message
    }

    override fun info(marker: Marker?, format: String, arg1: Any?, arg2: Any?) =
        delegate.info(marker?.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2).message
        }

    override fun info(marker: Marker?, format: String, vararg argArray: Any?) =
        delegate.info(marker?.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray).message
        }

    override fun info(marker: Marker?, message: String, cause: Throwable?) = delegate.info(marker?.asLognityMarker()) {
        MessageFormatter.format(message, cause).message
    }

    override fun isWarnEnabled(): Boolean = delegate.level <= Level.WARN
    override fun isWarnEnabled(marker: Marker?): Boolean = isWarnEnabled
    actual override fun warn(message: String) = delegate.info { message }

    override fun warn(format: String, arg: Any?) = delegate.warn {
        MessageFormatter.format(format, arg).message
    }

    override fun warn(format: String, arg1: Any?, arg2: Any?) = delegate.warn {
        MessageFormatter.format(format, arg1, arg2).message
    }

    override fun warn(format: String?, vararg arguments: Any?) = delegate.warn {
        MessageFormatter.arrayFormat(format, arguments).message
    }

    actual override fun warn(message: String, cause: Throwable) = delegate.warn {
        MessageFormatter.format(message, cause).message
    }

    override fun warn(marker: Marker?, message: String) = delegate.warn(marker?.asLognityMarker()) { message }

    override fun warn(marker: Marker?, format: String, arg: Any?) = delegate.warn(marker?.asLognityMarker()) {
        MessageFormatter.format(format, arg).message
    }

    override fun warn(marker: Marker?, format: String, arg1: Any?, arg2: Any?) =
        delegate.warn(marker?.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2).message
        }

    override fun warn(marker: Marker?, format: String, vararg argArray: Any?) =
        delegate.warn(marker?.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray).message
        }

    override fun warn(marker: Marker?, message: String, cause: Throwable?) = delegate.warn(marker?.asLognityMarker()) {
        MessageFormatter.format(message, cause).message
    }

    override fun isErrorEnabled(): Boolean = delegate.level <= Level.ERROR
    override fun isErrorEnabled(marker: Marker?): Boolean = isErrorEnabled
    actual override fun error(message: String) = delegate.info { message }

    override fun error(format: String, arg: Any?) = delegate.error {
        MessageFormatter.format(format, arg).message
    }

    override fun error(format: String, arg1: Any?, arg2: Any?) = delegate.error {
        MessageFormatter.format(format, arg1, arg2).message
    }

    override fun error(format: String?, vararg arguments: Any?) = delegate.error {
        MessageFormatter.arrayFormat(format, arguments).message
    }

    actual override fun error(message: String, cause: Throwable) = delegate.error {
        MessageFormatter.format(message, cause).message
    }

    override fun error(marker: Marker?, message: String) = delegate.error(marker?.asLognityMarker()) { message }

    override fun error(marker: Marker?, format: String, arg: Any?) = delegate.error(marker?.asLognityMarker()) {
        MessageFormatter.format(format, arg).message
    }

    override fun error(marker: Marker?, format: String, arg1: Any?, arg2: Any?) =
        delegate.error(marker?.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2).message
        }

    override fun error(marker: Marker?, format: String, vararg argArray: Any?) =
        delegate.error(marker?.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray).message
        }

    override fun error(marker: Marker?, message: String, cause: Throwable?) =
        delegate.error(marker?.asLognityMarker()) {
            MessageFormatter.format(message, cause).message
        }
}