package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.ConsoleAppender
import de.connect2x.lognity.config.appender.FileAppender
import de.connect2x.lognity.config.appender.SystemLogAppender
import de.connect2x.lognity.config.condition.LevelCondition
import de.connect2x.lognity.config.condition.MarkerCondition
import de.connect2x.lognity.config.condition.MessageCondition
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar

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