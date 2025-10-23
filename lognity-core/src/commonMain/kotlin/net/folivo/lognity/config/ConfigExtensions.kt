package net.folivo.lognity.config

import net.folivo.lognity.api.config.Config
import net.folivo.lognity.appender.ConsoleAppender
import net.folivo.lognity.appender.FileAppender

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