package de.connect2x.lognity.config

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.config.ConfigDsl
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.config.SerializableConfig.Companion.VERSION
import de.connect2x.lognity.config.appender.SerializableAppender
import de.connect2x.lognity.config.condition.AlwaysCondition
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.max

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

        private val extensionRegistrar: ConfigExtensionRegistrar = ConfigExtensionRegistrar()

        /**
         * Registers a configuration extension.
         *
         * Extensions are used to register custom appender and condition types so they
         * can be deserialized from JSON.
         *
         * @param extension the extension to register.
         */
        @SerializableConfigDsl
        infix fun uses(extension: ConfigExtension) = with(extension) {
            with(extensionRegistrar) {
                register()
            }
        }

        init { // Default implementations extension
            this uses ConfigExtension {
                registerConditionType<AlwaysCondition>()
                registerFormatterType("default", Formatter.default)
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json by lazy {
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                prettyPrintIndent = "\t"
                allowComments = true
                allowTrailingComma = true
                serializersModule = extensionRegistrar.createSerializersModule()
            }
        }

        /**
         * Loads and decodes a [SerializableConfig] from the given Source.
         *
         * The content must be JSON following the Lognity configuration schema. Unknown
         * keys and comments are allowed. The [version] is validated against [VERSION].
         *
         * @return the decoded [SerializableConfig].
         * @throws IllegalStateException when the version in the file is incompatible with [VERSION].
         */
        fun load(source: Source): SerializableConfig {
            val config = json.decodeFromString<SerializableConfig>(source.readString())
            check(config.version == VERSION) {
                "Incompatible lognity config version ${config.version}, expected $VERSION"
            }
            return config
        }
    }

    private val cachedConfig: Config by lazy {
        Config {
            level = this@SerializableConfig.level
            isEnabled = enabled
            for (appender in appenders) {
                val formatter = extensionRegistrar.formatterTypes[appender.formatter] ?: continue
                val factory = extensionRegistrar.appenderFactories[appender::class] ?: continue
                factory(appender, formatter)
            }
        }
    }

    @ConfigDsl
    context(builder: ConfigBuilder)
    fun applyConfig() = builder.setFrom(cachedConfig)

    fun asConfig(): Config = cachedConfig

    /**
     * Merges this configuration with another one.
     *
     * The resulting configuration will have:
     * - The maximum version of both.
     * - The higher log level of both.
     * - `enabled` set to true only if both are enabled.
     * - A combined list of appenders from both configurations.
     *
     * @param other the configuration to merge with.
     * @return a new [SerializableConfig] representing the merged result.
     */
    operator fun plus(other: SerializableConfig): SerializableConfig = copy( // @formatter:off
        version = max(version, other.version),
        level = when {
            level < other.level -> other.level
            else -> level
        },
        enabled = enabled && other.enabled,
        appenders = appenders + other.appenders
    ) // @formatter:on
}