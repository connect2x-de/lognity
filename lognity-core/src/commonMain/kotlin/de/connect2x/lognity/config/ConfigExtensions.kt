package de.connect2x.lognity.config

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.appender.ConsoleAppender
import de.connect2x.lognity.appender.FileAppender

/**
 * Returns all appenders of type [ConsoleAppender] defined in this [Config].
 *
 * This is a convenience property that filters [Config.appenders] to only console-based appenders.
 */
inline val Config.consoleAppenders: List<ConsoleAppender>
    get() = appenders.filterIsInstance<ConsoleAppender>()

/**
 * Returns all appenders of type [FileAppender] defined in this [Config].
 *
 * This is a convenience property that filters [Config.appenders] to only file-based appenders.
 */
inline val Config.fileAppenders: List<FileAppender>
    get() = appenders.filterIsInstance<FileAppender>()