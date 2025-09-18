/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.slf4j

import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import org.slf4j.Logger as Slf4jLogger

class LognitySlf4jLogger(
    val delegate: Logger
) : Slf4jLogger {
    override fun getName(): String = delegate.name

    override fun isTraceEnabled(): Boolean = delegate.level == Level.TRACE
    override fun isTraceEnabled(marker: Marker?): Boolean = isTraceEnabled
    override fun trace(message: String) = delegate.trace { message }

    override fun trace(format: String, arg: Any) = delegate.trace {
        MessageFormatter.format(format, arg)
    }

    override fun trace(format: String, arg1: Any, arg2: Any) = delegate.trace {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun trace(format: String?, vararg arguments: Any) = delegate.trace {
        MessageFormatter.arrayFormat(format, arguments)
    }

    override fun trace(message: String, cause: Throwable) = delegate.trace {
        MessageFormatter.format(message, cause)
    }

    override fun trace(marker: Marker, message: String) = delegate.trace(marker.asLognityMarker()) { message }

    override fun trace(marker: Marker, format: String, arg: Any) = delegate.trace(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg)
    }

    override fun trace(marker: Marker, format: String, arg1: Any, arg2: Any) =
        delegate.trace(marker.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2)
        }

    override fun trace(marker: Marker, format: String, vararg argArray: Any) =
        delegate.trace(marker.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray)
        }

    override fun trace(marker: Marker, message: String, cause: Throwable) = delegate.trace(marker.asLognityMarker()) {
        MessageFormatter.format(message, cause)
    }

    override fun isDebugEnabled(): Boolean = delegate.level <= Level.DEBUG
    override fun isDebugEnabled(marker: Marker?): Boolean = isDebugEnabled
    override fun debug(message: String) = delegate.debug { message }

    override fun debug(format: String, arg: Any) = delegate.debug {
        MessageFormatter.format(format, arg)
    }

    override fun debug(format: String, arg1: Any, arg2: Any) = delegate.debug {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun debug(format: String?, vararg arguments: Any) = delegate.debug {
        MessageFormatter.arrayFormat(format, arguments)
    }

    override fun debug(message: String, cause: Throwable) = delegate.debug {
        MessageFormatter.format(message, cause)
    }

    override fun debug(marker: Marker, message: String) = delegate.debug(marker.asLognityMarker()) { message }

    override fun debug(marker: Marker, format: String, arg: Any) = delegate.debug(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg)
    }

    override fun debug(marker: Marker, format: String, arg1: Any, arg2: Any) =
        delegate.debug(marker.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2)
        }

    override fun debug(marker: Marker, format: String, vararg argArray: Any) =
        delegate.debug(marker.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray)
        }

    override fun debug(marker: Marker, message: String, cause: Throwable) = delegate.debug(marker.asLognityMarker()) {
        MessageFormatter.format(message, cause)
    }

    override fun isInfoEnabled(): Boolean = delegate.level <= Level.INFO
    override fun isInfoEnabled(marker: Marker?): Boolean = isInfoEnabled
    override fun info(message: String) = delegate.info { message }

    override fun info(format: String, arg: Any) = delegate.info {
        MessageFormatter.format(format, arg)
    }

    override fun info(format: String, arg1: Any, arg2: Any) = delegate.info {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun info(format: String?, vararg arguments: Any) = delegate.info {
        MessageFormatter.arrayFormat(format, arguments)
    }

    override fun info(message: String, cause: Throwable) = delegate.info {
        MessageFormatter.format(message, cause)
    }

    override fun info(marker: Marker, message: String) = delegate.info(marker.asLognityMarker()) { message }

    override fun info(marker: Marker, format: String, arg: Any) = delegate.info(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg)
    }

    override fun info(marker: Marker, format: String, arg1: Any, arg2: Any) = delegate.info(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun info(marker: Marker, format: String, vararg argArray: Any) = delegate.info(marker.asLognityMarker()) {
        MessageFormatter.arrayFormat(format, argArray)
    }

    override fun info(marker: Marker, message: String, cause: Throwable) = delegate.info(marker.asLognityMarker()) {
        MessageFormatter.format(message, cause)
    }

    override fun isWarnEnabled(): Boolean = delegate.level <= Level.WARN
    override fun isWarnEnabled(marker: Marker?): Boolean = isWarnEnabled
    override fun warn(message: String) = delegate.info { message }

    override fun warn(format: String, arg: Any) = delegate.warn {
        MessageFormatter.format(format, arg)
    }

    override fun warn(format: String, arg1: Any, arg2: Any) = delegate.warn {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun warn(format: String?, vararg arguments: Any) = delegate.warn {
        MessageFormatter.arrayFormat(format, arguments)
    }

    override fun warn(message: String, cause: Throwable) = delegate.warn {
        MessageFormatter.format(message, cause)
    }

    override fun warn(marker: Marker, message: String) = delegate.warn(marker.asLognityMarker()) { message }

    override fun warn(marker: Marker, format: String, arg: Any) = delegate.warn(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg)
    }

    override fun warn(marker: Marker, format: String, arg1: Any, arg2: Any) = delegate.warn(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun warn(marker: Marker, format: String, vararg argArray: Any) = delegate.warn(marker.asLognityMarker()) {
        MessageFormatter.arrayFormat(format, argArray)
    }

    override fun warn(marker: Marker, message: String, cause: Throwable) = delegate.warn(marker.asLognityMarker()) {
        MessageFormatter.format(message, cause)
    }

    override fun isErrorEnabled(): Boolean = delegate.level <= Level.ERROR
    override fun isErrorEnabled(marker: Marker?): Boolean = isErrorEnabled
    override fun error(message: String) = delegate.info { message }

    override fun error(format: String, arg: Any) = delegate.error {
        MessageFormatter.format(format, arg)
    }

    override fun error(format: String, arg1: Any, arg2: Any) = delegate.error {
        MessageFormatter.format(format, arg1, arg2)
    }

    override fun error(format: String?, vararg arguments: Any) = delegate.error {
        MessageFormatter.arrayFormat(format, arguments)
    }

    override fun error(message: String, cause: Throwable) = delegate.error {
        MessageFormatter.format(message, cause)
    }

    override fun error(marker: Marker, message: String) = delegate.error(marker.asLognityMarker()) { message }

    override fun error(marker: Marker, format: String, arg: Any) = delegate.error(marker.asLognityMarker()) {
        MessageFormatter.format(format, arg)
    }

    override fun error(marker: Marker, format: String, arg1: Any, arg2: Any) =
        delegate.error(marker.asLognityMarker()) {
            MessageFormatter.format(format, arg1, arg2)
        }

    override fun error(marker: Marker, format: String, vararg argArray: Any) =
        delegate.error(marker.asLognityMarker()) {
            MessageFormatter.arrayFormat(format, argArray)
        }

    override fun error(marker: Marker, message: String, cause: Throwable) = delegate.error(marker.asLognityMarker()) {
        MessageFormatter.format(message, cause)
    }
}

fun Logger.asSlf4jLogger(): Slf4jLogger = when (this) {
    is Slf4jLognityLogger -> delegate
    else -> LognitySlf4jLogger(this)
}