package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.ConsoleAppender
import de.connect2x.lognity.config.appender.FileAppender
import de.connect2x.lognity.config.appender.RollingFileAppender
import de.connect2x.lognity.config.appender.SystemConsoleAppender
import de.connect2x.lognity.config.appender.SystemLogAppender
import de.connect2x.lognity.config.condition.CoroutineNameCondition
import de.connect2x.lognity.config.condition.LevelCondition
import de.connect2x.lognity.config.condition.LoggerNameCondition
import de.connect2x.lognity.config.condition.MarkerCondition
import de.connect2x.lognity.config.condition.MessageCondition
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import de.connect2x.lognity.config.override.CoroutineNameOverrideCondition
import de.connect2x.lognity.config.override.LoggerNameOverrideCondition
import de.connect2x.lognity.config.override.MarkerOverrideCondition

internal expect fun ConfigExtensionRegistrar.registerPlatformAppenderTypes()

/**
 * Core configuration extension for Lognity.
 *
 * This extension registers the default appenders and conditions provided by the core library.
 *
 * Registered appenders:
 * - [ConsoleAppender]: Writes log messages to the console (stdout).
 * - [SystemConsoleAppender]: Writes log messages to the system console (stdout & stderr) if available.
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
            consoleAppender(config.pattern.resolve(), formatter, config.filter.resolve(), config.name.resolve())
        }
        // System specific console appender, which may provide additional functionality
        registerAppenderType<SystemConsoleAppender> { config, formatter ->
            systemConsoleAppender(config.pattern.resolve(), formatter, config.filter.resolve(), config.name.resolve())
        }
        // System specific appender for the systems underlying log mechanism if present, console otherwise
        registerAppenderType<SystemLogAppender> { config, formatter ->
            systemLogAppender(config.pattern.resolve(), formatter, config.filter.resolve(), config.name.resolve())
        }
        // Fixed path
        registerAppenderType<FileAppender> { config, formatter ->
            fileAppender(
                config.path.resolve(),
                config.pattern.resolve(),
                formatter,
                config.filter.resolve(),
                config.name.resolve()
            )
        }
        // Rolling file
        registerAppenderType<RollingFileAppender> { config, formatter ->
            rollingFileAppender(
                config.basePath.resolve(),
                config.pattern.resolve(),
                formatter,
                config.filter.resolve(),
                config.name.resolve(),
                config.fileCount.resolve(),
                config.maxFileSize.resolve(),
                config.useTimestamps.resolve(),
                config.deleteExisting.resolve(),
                config.latestSuffix.resolve()
            )
        }
        registerPlatformAppenderTypes()
    }

    private fun ConfigExtensionRegistrar.registerConditionTypes() {
        registerConditionType<LevelCondition>()
        registerConditionType<MarkerCondition>()
        registerConditionType<MessageCondition>()
        registerConditionType<LoggerNameCondition>()
        registerConditionType<CoroutineNameCondition>()
    }

    private fun ConfigExtensionRegistrar.registerOverrideConditionTypes() {
        registerOverrideConditionType<LoggerNameOverrideCondition>()
        registerOverrideConditionType<MarkerOverrideCondition>()
        registerOverrideConditionType<CoroutineNameOverrideCondition>()
    }

    override fun ConfigExtensionRegistrar.register() {
        registerAppenderTypes()
        registerConditionTypes()
        registerOverrideConditionTypes()
    }
}