package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.EventAppender
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import de.connect2x.lognity.appender.EventAppender as EventAppenderImpl

internal actual fun ConfigExtensionRegistrar.registerPlatformAppenderTypes() {
    registerAppenderType<EventAppender> { config, formatter ->
        appender(EventAppenderImpl(config.pattern, formatter, config.filter, config.name))
    }
}