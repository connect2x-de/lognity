package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.ConsoleAppender
import de.connect2x.lognity.config.appender.FileAppender
import de.connect2x.lognity.config.condition.LevelCondition
import de.connect2x.lognity.config.condition.MarkerCondition
import de.connect2x.lognity.config.condition.MessageCondition

object CoreConfig {
    fun install() {
        installAppenderTypes()
        installConditionTypes()
    }

    private fun installAppenderTypes() = with(SerializableConfig) {
        registerAppenderType<ConsoleAppender> { config, formatter ->
            consoleAppender(config.pattern, formatter, config.filter)
        }
        registerAppenderType<FileAppender> { config, formatter ->
            if (config.isRolling) {
                rollingFileAppender(config.pattern, formatter, config.filter, config.path)
                return@registerAppenderType
            }
            fileAppender(config.pattern, formatter, config.filter, config.path)
        }
    }

    private fun installConditionTypes() = with(SerializableConfig) {
        registerConditionType<LevelCondition>()
        registerConditionType<MarkerCondition>()
        registerConditionType<MessageCondition>()
    }
}