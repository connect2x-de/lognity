package net.folivo.lognity.config

import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.config.config
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.config.SerializableConfig.Companion.VERSION

/**
 * Serializable representation of the Lognity configuration.
 *
 * This mirrors the JSON schema used to configure Lognity. It can be loaded from
 * a JSON source and then applied to a ConfigBuilder or converted into a Config.
 *
 * @property version configuration format version. Must match [VERSION].
 * @property level default log level.
 * @property enabled whether logging is enabled globally.
 * @property appenders list of configured appenders.
 */
@Serializable
data class SerializableConfig( // @formatter:off
    val version: Int = VERSION,
    val level: Level = Level.default,
    val enabled: Boolean = true,
    val appenders: List<SerializableAppender> = emptyList(),
) { // @formatter:on
    /**
     * Companion for utilities and constants related to [SerializableConfig].
     */
    companion object {
        /**
         * Configuration format version supported by this library.
         * Config files must specify the same version to be accepted.
         */
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
                SerializableFilter.Condition.serializersModule
            // @formatter:on
        }

        /**
         * Loads and decodes a [SerializableConfig] from the given Source.
         *
         * The content must be JSON following the Lognity configuration schema. Unknown
         * keys and comments are allowed. The [version] is validated against [VERSION].
         *
         * @throws IllegalStateException when the version in the file is incompatible.
         */
        fun load(source: Source): SerializableConfig {
            val config = json.decodeFromString<SerializableConfig>(source.readString())
            check(config.version == VERSION) {
                "Incompatible lognity config version ${config.version}, expected at least $VERSION"
            }
            return config
        }
    }

    /**
     * Applies this configuration to the given ConfigBuilder.
     *
     * - Sets builder-level properties like level and isEnabled.
     * - Registers appenders defined in this config, resolving formatter names
     *   using the provided [formatters] map.
     *
     * @param formatters map that resolves formatter identifiers used by this config
     */
    context(builder: ConfigBuilder) fun applyConfig(
        formatters: Map<String, Formatter> = mapOf("default" to Formatter.default)
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

    /**
     * Creates an immutable Config from this serializable configuration.
     *
     * This is a convenience wrapper around config { applyConfig(...) }.
     *
     * @param formatters map that resolves formatter identifiers used by this config
     */
    fun createConfig(
        formatters: Map<String, Formatter> = mapOf("default" to Formatter.default)
    ): Config = config { applyConfig(formatters) }
}