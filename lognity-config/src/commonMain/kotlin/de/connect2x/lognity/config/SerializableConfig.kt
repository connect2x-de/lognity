package de.connect2x.lognity.config

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.config.SerializableConfig.Companion.VERSION
import de.connect2x.lognity.config.appender.SerializableAppender
import de.connect2x.lognity.config.condition.AlwaysCondition
import de.connect2x.lognity.config.condition.AndCondition
import de.connect2x.lognity.config.condition.ExactlyOneCondition
import de.connect2x.lognity.config.condition.OrCondition
import de.connect2x.lognity.config.condition.SerializableCondition
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.extension.ConfigExtensionRegistrar
import de.connect2x.lognity.config.override.SerializableOverride
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
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
    val level: RefOrValue<Level> = RefOrValue.Value(Level.default),
    val enabled: RefOrValue<Boolean> = RefOrValue.Value(true),
    val appenders: List<SerializableAppender> = emptyList(),
    val conditions: List<SerializableCondition> = emptyList(), // Globally accessible conditions/templates
    val overrides: List<SerializableOverride> = emptyList() // Global overrides
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

        internal val extensionRegistrar: ConfigExtensionRegistrar = ConfigExtensionRegistrar()

        /**
         * Registers a configuration extension.
         *
         * Extensions are used to register custom appender and condition types so they
         * can be deserialized from JSON.
         *
         * @param extension the extension to register.
         */
        infix fun uses(extension: ConfigExtension) = with(extension) {
            with(extensionRegistrar) {
                register()
            }
        }

        init { // Default implementations extension
            this uses ConfigExtension {
                registerBuiltinEnum("Level", Level.entries)
                registerConditionType<AlwaysCondition>()
                registerConditionType<OrCondition>()
                registerConditionType<AndCondition>()
                registerConditionType<ExactlyOneCondition>()
                registerFormatterType("default", Formatter::default)
                // Register template provider for pre-defined conditions
                registerTemplateProvider("conditions") { name ->
                    for (condition in conditions) {
                        if (condition.name.resolve() != name) continue
                        return@registerTemplateProvider condition
                    }
                    error("Could not find pre-defined condition named '$name'")
                }
            }
        }

        @InternalConfigApi
        val serializersModule: SerializersModule by lazy(extensionRegistrar::createSerializersModule)

        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json by lazy {
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                prettyPrintIndent = "\t"
                allowComments = true
                allowTrailingComma = true
                serializersModule = this@Companion.serializersModule
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
            level = getCombinedLevel()
            isEnabled = enabled.resolve()
            for (appender in appenders) {
                val formatter = extensionRegistrar.formatterFactories[appender.formatter.resolve()] ?: continue
                val factory = extensionRegistrar.appenderFactories[appender::class] ?: continue
                factory(appender, formatter())
            }
            for (override in overrides) {
                override {
                    applyWhen(override.condition)
                    level = override.level?.resolve()
                    enableState = override.enableState?.resolve()
                }
            }
        }
    }

    init { // Propagate a reference down the hierarchy
        for (appender in appenders) appender.config = this
        for (override in overrides) override.config = this
    }

    /**
     * Calculates the combined log level.
     *
     * @return the resolved log level.
     */
    fun getCombinedLevel(): Level {
        val defaultLevel = Level.default
        val level = this.level.resolve()
        return if (level < defaultLevel) level
        else defaultLevel
    }

    /**
     * Applies this configuration to the given [ConfigBuilder].
     */
    context(builder: ConfigBuilder)
    fun applyConfig() = builder.setFrom(cachedConfig)

    /**
     * Returns this configuration as a [Config] instance.
     *
     * @return the [Config] instance.
     */
    fun asConfig(): Config = cachedConfig

    /**
     * Merges this configuration with another one.
     *
     * The resulting configuration will have:
     * - The maximum version of both.
     * - The higher log level of both.
     * - `enabled` set to true only if both are enabled.
     * - A combined list of appenders from both configurations.
     * - A combined list of overrides from both configurations.
     *
     * @param other the configuration to merge with.
     * @return a new [SerializableConfig] representing the merged result.
     */
    operator fun plus(other: SerializableConfig): SerializableConfig = copy( // @formatter:off
        version = max(version, other.version),
        level = RefOrValue.Value(when {
            level.resolve() < other.level.resolve() -> other.level.resolve()
            else -> level.resolve()
        }),
        enabled = RefOrValue.Value(enabled.resolve() && other.enabled.resolve()),
        appenders = appenders + other.appenders,
        overrides = overrides + other.overrides
    ) // @formatter:on
}