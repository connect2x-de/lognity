package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.ConsoleAppender
import de.connect2x.lognity.config.appender.FileAppender
import de.connect2x.lognity.config.appender.SystemLogAppender
import de.connect2x.lognity.config.condition.LevelCondition
import de.connect2x.lognity.config.condition.MarkerCondition
import de.connect2x.lognity.config.condition.MessageCondition
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar

/**
 * Core configuration extension for Lognity.
 *
 * This extension registers the default appenders and conditions provided by the core library.
 *
 * Registered appenders:
 * - [ConsoleAppender]: Writes log messages to the console (stdout/stderr).
 * - [SystemLogAppender]: Writes log messages to the system's underlying log mechanism.
 * - [FileAppender]: Writes log messages to a file (supports rolling files).
 *
 * Registered conditions:
 * - [LevelCondition]: Filters log messages based on their log level.
 * - [MarkerCondition]: Filters log messages based on their marker.
 * - [MessageCondition]: Filters log messages based on their content.
 */
object CoreConfigExtension : ConfigExtension {
    private fun ConfigExtensionRegistrar.registerAppenderTypes() {
        // Regular console appender which writes to stdout and stderr
        registerAppenderType<ConsoleAppender> { config, formatter ->
            consoleAppender(config.pattern, formatter, config.filter, config.name)
        }
        // System specific appender for the systems underlying log mechanism if present, console otherwise
        registerAppenderType<SystemLogAppender> { config, formatter ->
            systemLogAppender(config.pattern, formatter, config.filter, config.name)
        }
        // Fixed path or rolling file appender
        registerAppenderType<FileAppender> { config, formatter ->
            if (config.isRolling) {
                rollingFileAppender(config.path, config.pattern, formatter, config.filter, config.name)
                return@registerAppenderType
            }
            fileAppender(config.path, config.pattern, formatter, config.filter, config.name)
        }
    }

    private fun ConfigExtensionRegistrar.registerConditionTypes() {
        registerConditionType<LevelCondition>()
        registerConditionType<MarkerCondition>()
        registerConditionType<MessageCondition>()
    }

    override fun ConfigExtensionRegistrar.register() {
        registerAppenderTypes()
        registerConditionTypes()
    }
}