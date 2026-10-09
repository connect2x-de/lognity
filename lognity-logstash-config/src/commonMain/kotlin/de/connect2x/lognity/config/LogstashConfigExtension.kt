package de.connect2x.lognity.config

import de.connect2x.lognity.config.appender.LogstashAppender
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import de.connect2x.lognity.logstash.LogstashAppender as LogstashAppenderImpl

/** Registers the Logstash TCP appender for use in serializable configurations. */
object LogstashConfigExtension : ConfigExtension {
    override fun ConfigExtensionRegistrar.register() {
        registerAppenderType<LogstashAppender> { config, formatter ->
            val appender = LogstashAppenderImpl(
                config = config.asLogstashConfig(),
                encoder = config.encoder.resolveTemplate(config.config).asEncoder(config.config),
                pattern = config.pattern.resolveTemplate(config.config),
                formatter = formatter,
                filter = config.filter.resolveTemplate(config.config),
                name = config.name.resolveTemplate(config.config),
                autoStart = false
            )
            try {
                appender(appender)
                appender.start()
            } catch (cause: Throwable) {
                appender.stop()
                throw cause
            }
        }
    }
}