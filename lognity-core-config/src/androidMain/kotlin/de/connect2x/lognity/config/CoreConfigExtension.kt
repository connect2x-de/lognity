package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.LogcatAppender
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import de.connect2x.lognity.appender.LogcatAppender as LogcatAppenderImpl

internal actual fun ConfigExtensionRegistrar.registerPlatformAppenderTypes() {
    registerAppenderType<LogcatAppender> { config, formatter ->
        appender(
            LogcatAppenderImpl(
                config.pattern.resolve(), formatter, config.filter.resolve(), config.name.resolve()
            )
        )
    }
}