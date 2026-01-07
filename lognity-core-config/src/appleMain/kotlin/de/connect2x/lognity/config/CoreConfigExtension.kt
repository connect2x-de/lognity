package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.OsAppender
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import de.connect2x.lognity.appender.OsAppender as OsAppenderImpl

internal actual fun ConfigExtensionRegistrar.registerPlatformAppenderTypes() {
    registerAppenderType<OsAppender> { config, formatter ->
        appender(OsAppenderImpl(config.pattern, formatter, config.filter, config.name))
    }
}