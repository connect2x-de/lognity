package de.connect2x.lognity.config

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.config.ConfigDsl
import de.connect2x.lognity.api.config.config
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.config.SerializableConfig.Companion.VERSION
import de.connect2x.lognity.config.appender.AppenderFactory
import de.connect2x.lognity.config.appender.SerializableAppender
import de.connect2x.lognity.config.condition.AlwaysCondition
import de.connect2x.lognity.config.condition.SerializableCondition
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

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

        @PublishedApi
        internal var appenderTypes: PolymorphicModuleBuilder<SerializableAppender>.() -> Unit = {}

        @PublishedApi
        internal val appenderFactories: HashMap<KClass<out SerializableAppender>, AppenderFactory<SerializableAppender>> =
            HashMap()

        @PublishedApi
        internal var conditionTypes: PolymorphicModuleBuilder<SerializableCondition>.() -> Unit = {}

        @Suppress("UNCHECKED_CAST")
        inline fun <reified A : SerializableAppender> registerAppenderType(noinline factory: AppenderFactory<A>) {
            val oldCallback = appenderTypes
            appenderTypes = {
                oldCallback()
                subclass(A::class)
            }
            appenderFactories[A::class] = factory as AppenderFactory<SerializableAppender>
        }

        inline fun <reified C : SerializableCondition> registerConditionType() {
            val oldCallback = conditionTypes
            conditionTypes = {
                oldCallback()
                subclass(C::class)
            }
        }

        init {
            registerConditionType<AlwaysCondition>() // The always condition is built-in as a default
        }

        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json by lazy {
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                prettyPrintIndent = "\t"
                allowComments = true
                allowTrailingComma = true
                // @formatter:off
                serializersModule = SerializersModule {
                    polymorphic(SerializableAppender::class) { appenderTypes() }
                    polymorphic(SerializableCondition::class) { conditionTypes() }
                }
                // @formatter:on
            }
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
    @ConfigDsl
    context(builder: ConfigBuilder)
    fun applyConfig(
        formatters: Map<String, Formatter> = mapOf("default" to Formatter.default)
    ) = with(builder) {
        level = this@SerializableConfig.level
        isEnabled = enabled
        loop@ for (appender in appenders) onlyOn(appender.platforms) {
            val formatter = formatters[appender.formatter] ?: continue@loop
            val factory = appenderFactories[appender::class] ?: continue@loop
            factory(appender, formatter)
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