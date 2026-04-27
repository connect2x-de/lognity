package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.FileAppender
import de.connect2x.lognity.config.appender.RollingFileAppender
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar

internal actual fun ConfigExtensionRegistrar.registerNonWebAppenderTypes() {
    // Fixed path
    registerAppenderType<FileAppender> { config, formatter ->
        fileAppender( // @formatter:off
            config.path.resolve(),
            config.pattern.resolve(),
            formatter,
            config.filter.resolve(),
            config.name.resolve()
        ) // @formatter:on
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
}