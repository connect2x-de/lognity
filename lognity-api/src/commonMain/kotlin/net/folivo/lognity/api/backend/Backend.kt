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

package net.folivo.lognity.api.backend

import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.format.Formatter
import kotlinx.io.files.Path
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Interface representing a logging backend implementation.
 * The backend is responsible for creating loggers, markers, and appenders,
 * as well as providing default configuration for the logging system.
 */
interface Backend {
    @OptIn(ExperimentalAtomicApi::class)
    companion object {
        private val _current: AtomicReference<Backend> = AtomicReference(NoopBackend)

        /**
         * The current active logging backend instance.
         * This property allows getting and setting the global logging backend.
         */
        var current: Backend
            get() = _current.load()
            set(value) {
                _current.store(value)
            }
    }

    /**
     * The name of this logging backend implementation.
     */
    val name: String

    /**
     * The default log level used by this backend when not explicitly specified.
     * This is used as the initial level for new loggers.
     */
    val defaultLevel: Level

    /**
     * The default log formatter used by this backend when not explicitly specified.
     * This is used for formatting log messages in appenders.
     */
    val defaultFormatter: Formatter

    /**
     * The default logger configuration specification used by this backend.
     * This is used when creating new loggers without an explicit configuration.
     */
    var defaultConfigSpec: ConfigBuilder.() -> Unit

    /**
     * Creates a new log marker with the specified parameters.
     *
     * @param key The internal key the marker is referenced by in cache.
     * @param name The name of the marker that is printed when {{marker}} is used.
     * @param isEnabled When true, all messages with this marker will be logged.
     * @return A new log marker instance.
     */
    fun createMarker(key: String, name: String, isEnabled: Boolean): Marker

    /**
     * Creates a new logger with the specified name and configuration.
     *
     * @param name The name of the logger, typically following a hierarchical naming convention.
     * @param configSpec The configuration specification for the logger. Defaults to the backend's default configuration.
     * @return A new logger instance.
     */
    fun createLogger(name: String, configSpec: ConfigBuilder.() -> Unit = defaultConfigSpec): Logger

    /**
     * Creates a new file appender that writes log messages to a file.
     *
     * @param pattern The formatting pattern to apply to all messages passed to the appender.
     * @param formatter The formatter used to apply the pattern to each message.
     * @param filter The filter to apply to determine whether a message should be logged.
     * @param path The file path where log messages will be written.
     * @return A new file appender instance.
     */
    fun createFileAppender(pattern: String, formatter: Formatter, filter: Filter, path: Path): Appender

    /**
     * Creates a new console appender that writes log messages to the standard output.
     *
     * @param pattern The formatting pattern to apply to all messages passed to the appender.
     * @param formatter The formatter used to apply the pattern to each message.
     * @param filter The filter to apply to determine whether a message should be logged.
     * @return A new console appender instance.
     */
    fun createConsoleAppender(pattern: String, formatter: Formatter, filter: Filter): Appender

    /**
     * Creates a platform-specific console appender that writes log messages to the
     * appropriate console for the current platform.
     *
     * The default implementation simply calls [createConsoleAppender], but platform-specific
     * implementations may override this to provide better integration with the platform's
     * native logging facilities.
     *
     * @param pattern The formatting pattern to apply to all messages passed to the appender.
     * @param formatter The formatter used to apply the pattern to each message.
     * @param filter The filter to apply to determine whether a message should be logged.
     * @return A new platform-specific console appender instance.
     */
    fun createSystemAppender(pattern: String, formatter: Formatter, filter: Filter): Appender =
        createConsoleAppender(pattern, formatter, filter)
}
