package net.folivo.lognity.config

import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Context
import net.folivo.lognity.api.logger.ContextBuilder
import net.folivo.lognity.api.logger.Level

@Serializable
data class SerializableConfig( // @formatter:off
    val version: Int = VERSION,
    val level: Level = Level.default(),
    val enabled: Boolean = true,
    val appenders: List<SerializableAppender> = emptyList(),
    val context: Map<String, SerializableValue<*>> = emptyMap()
) { // @formatter:on
    companion object {
        const val VERSION: Int = 1

        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            prettyPrintIndent = "\t"
            allowComments = true
            allowTrailingComma = true
            // @formatter:off
            serializersModule = SerializableAppender.serializersModule +
                SerializableFilter.Condition.serializersModule +
                SerializableValue.serializersModule
            // @formatter:on
        }

        fun load(source: Source): SerializableConfig {
            val config = json.decodeFromString<SerializableConfig>(source.readString())
            check(config.version == VERSION) {
                "Incompatible lognity config version ${config.version}, expected at least $VERSION"
            }
            return config
        }
    }

    context(builder: ConfigBuilder) fun applyConfig(
        formatters: Map<String, Formatter> = mapOf("default" to Backend.current.defaultFormatter)
    ) {
        builder.level = level
        builder.isEnabled = enabled
        for (appender in appenders) when (appender) {
            is SerializableAppender.Console -> {
                val formatter = requireNotNull(formatters[appender.formatter])
                builder.platformConsoleAppender(appender.pattern, formatter, appender.filter)
            }

            is SerializableAppender.File -> {
                val formatter = requireNotNull(formatters[appender.formatter])
                builder.fileAppender(appender.pattern, formatter, appender.filter, appender.path)
            }
        }
    }

    fun createConfig(
        formatters: Map<String, Formatter> = mapOf("default" to Backend.current.defaultFormatter)
    ): Config = ConfigBuilder().apply { applyConfig(formatters) }.build()

    @Suppress("UNCHECKED_CAST")
    context(builder: ContextBuilder) fun applyContext() {
        for ((name, value) in context) {
            builder.value(value.createKey(name) as Context.Key<Any>, value.value)
        }
    }

    fun createContext(): Context = ContextBuilder().apply { applyContext() }.build()
}