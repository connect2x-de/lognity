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

package net.folivo.lognity.api.config

import net.folivo.lognity.api.Level
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter

/**
 * A builder class for creating a new [Config] instance
 * using a simple DSL.
 */
class ConfigBuilder {
    /**
     * The initial log level used by the logger instance(s)
     * using this config.
     */
    var level: Level = Level.default()

    /**
     * The initial enable state applied to the logger instance(s)
     * using this config.
     */
    var isEnabled: Boolean = true

    @PublishedApi
    internal val appenders: ArrayList<Appender> = ArrayList()

    /**
     * Copies all config properties from the given [Config] instance
     * into this builder instance.
     *
     * @param config The config from which to copy all properties into this builder instance.
     * @return This builder instance.
     */
    fun setFrom(config: Config): ConfigBuilder {
        level = config.initialLevel
        isEnabled = config.initialEnableState
        appenders += config.appenders
        return this
    }

    /**
     * Adds a new appender instance to this logger config if not already present.
     *
     * @param appender The appender instance to add to this config.
     */
    fun appender(appender: Appender) {
        require(appender !in appenders) { "Appender is already present" }
        appenders += appender
    }

    /**
     * Adds a new console appender to this logger config.
     *
     * @param pattern The formatting pattern to apply to all messages passed to the new appender.
     * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
     * @param filter The filter to apply for every message to determine whether it should be logged.
     */
    fun consoleAppender( // @formatter:off
        pattern: String,
        formatter: Formatter = Formatter.default,
        filter: Filter = Filter.always
    ) { // @formatter:on
        appenders += Backend.current.createConsoleAppender(pattern, formatter, filter)
    }

    /**
     * Adds a new platform console appender to this logger config.
     * This will automatically use the appropriate console appender for the
     * underlying platform instead of raw stdio if available.
     *
     * @param pattern The formatting pattern to apply to all messages passed to the new appender.
     * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
     * @param filter The filter to apply for every message to determine whether it should be logged.
     */
    fun platformConsoleAppender( // @formatter:off
        pattern: String,
        formatter: Formatter = Formatter.default,
        filter: Filter = Filter.always
    ) { // @formatter:on
        appenders += Backend.current.createSystemAppender(pattern, formatter, filter)
    }

    /**
     * Adds a new file appender to this logger config.
     *
     * @param pattern The formatting pattern to apply to all messages passed to the new appender.
     * @param path The file path at which to save the log.
     * @param formatter The formatter used to apply the specified pattern to each message. See [Formatter].
     * @param filter The filter to apply for every message to determine whether it should be logged.
     */
    fun fileAppender( // @formatter:off
        pattern: String,
        formatter: Formatter = Formatter.default,
        filter: Filter = Filter.always,
        path: String
    ) { // @formatter:on
        appenders += Backend.current.createFileAppender(pattern, formatter, filter, path)
    }

    fun build(): Config = Config(level, isEnabled, appenders)
}