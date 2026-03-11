package de.connect2x.lognity.config.extension

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.config.SerializableConfigDsl
import de.connect2x.lognity.config.appender.AppenderFactory
import de.connect2x.lognity.config.appender.SerializableAppender
import de.connect2x.lognity.config.condition.SerializableCondition
import de.connect2x.lognity.config.override.SerializableOverrideCondition
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.concurrent.atomics.AtomicReference
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Registrar for custom configuration extensions.
 *
 * This class provides methods to register custom appender and condition types,
 * allowing them to be used in the polymorphic serialization of the configuration.
 */
@SerializableConfigDsl
class ConfigExtensionRegistrar internal constructor() {
    @PublishedApi
    internal val appenderTypes: AtomicReference<PolymorphicModuleBuilder<SerializableAppender>.() -> Unit> =
        AtomicReference {}

    @PublishedApi
    internal val appenderFactories: SharedHashMap<KClass<out SerializableAppender>, AppenderFactory<SerializableAppender>> =
        SharedHashMap()

    @PublishedApi
    internal val conditionTypes: AtomicReference<PolymorphicModuleBuilder<SerializableCondition>.() -> Unit> =
        AtomicReference {}

    @PublishedApi
    internal val overrideConditionTypes: AtomicReference<PolymorphicModuleBuilder<SerializableOverrideCondition>.() -> Unit> =
        AtomicReference {}

    internal val formatterFactories: SharedHashMap<String, () -> Formatter> = SharedHashMap()
    internal val providers: SharedHashMap<String, Provider<Any?>> = SharedHashMap()
    internal val templateProviders: SharedHashMap<String, TemplateProvider<Any?>> = SharedHashMap()
    internal val builtinProviders: SharedHashMap<String, BuiltinProvider<Any?>> = SharedHashMap()

    /**
     * Finds a previously registered built-in provider by its prefix.
     *
     * Built-in providers are lightweight resolvers that map a textual token
     * (the part after the prefix) to a computed value.
     *
     * @param prefix the prefix the provider was registered with.
     * @return the provider cast to [T] if present, or null when not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> findBuiltinProvider(prefix: String): BuiltinProvider<T>? {
        return builtinProviders[prefix] as? BuiltinProvider<T>
    }

    /**
     * Registers a built-in provider that is addressed by a textual prefix.
     *
     * Examples for prefixes could be "env" or "sys". The concrete value is
     * resolved by the provider from the remaining token after the prefix.
     *
     * @param prefix unique, case-sensitive prefix used in configuration.
     * @param provider function that resolves a value from the token that
     * follows the prefix.
     */
    fun registerBuiltinProvider(prefix: String, provider: BuiltinProvider<Any?>) {
        builtinProviders[prefix] = provider
    }

    /**
     * Registers all constants of an enum as a built-in provider.
     *
     * The created provider resolves the token to the enum constant with the
     * matching [Enum.name].
     *
     * @param prefix prefix under which the enum values are exposed.
     * @param values iterable of enum constants to resolve from.
     */
    fun <E : Enum<E>> registerBuiltinEnum(prefix: String, values: Iterable<E>) {
        registerBuiltinProvider(prefix) { name ->
            values.find { entry -> entry.name == name }
        }
    }

    /**
     * Looks up a template provider for the given prefix.
     *
     * Template providers can produce values based on a template string and the
     * current configuration context.
     *
     * @param prefix the prefix the provider was registered with.
     * @return the provider cast to [T] if present, or null otherwise.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> findTemplateProvider(prefix: String): TemplateProvider<T>? {
        return templateProviders[prefix] as? TemplateProvider<T>
    }

    /**
     * Registers a template provider under the given prefix.
     *
     * Template providers receive the template string and the current
     * [de.connect2x.lognity.config.SerializableConfig] as receiver to compute
     * a value.
     *
     * @param prefix unique, case-sensitive prefix used to address this provider.
     * @param provider the provider function.
     */
    fun registerTemplateProvider(prefix: String, provider: TemplateProvider<Any?>) {
        templateProviders[prefix] = provider
    }

    /**
     * Finds a previously registered named provider.
     *
     * @param name the name the provider was registered with.
     * @return the provider cast to [T] if present, or null when not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> findProvider(name: String): Provider<T>? {
        return providers[name] as? Provider<T>
    }

    /**
     * Registers a configuration provider.
     *
     * @param name the name of the provider.
     * @param provider the provider function.
     */
    fun registerProvider(name: String, provider: Provider<Any?>) {
        providers[name] = provider
    }

    /**
     * Registers a formatter type.
     *
     * @param name the name of the formatter type.
     * @param factory Factory function for creating new instances of the given formatter.
     */
    fun registerFormatterType(name: String, factory: () -> Formatter) {
        formatterFactories[name] = factory
    }

    /**
     * Registers a custom appender type and its corresponding factory.
     *
     * @param A the type of the serializable appender to register.
     * @param factory the factory responsible for creating the appender instance.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified A : SerializableAppender> registerAppenderType(noinline factory: AppenderFactory<A>) {
        contract {
            callsInPlace(factory, InvocationKind.AT_MOST_ONCE)
        }
        val oldCallback = appenderTypes.load()
        appenderTypes.store {
            oldCallback()
            subclass(A::class)
        }
        appenderFactories[A::class] = factory as AppenderFactory<SerializableAppender>
    }

    /**
     * Registers a custom condition type.
     *
     * @param C the type of the serializable condition to register.
     */
    inline fun <reified C : SerializableCondition> registerConditionType() {
        val oldCallback = conditionTypes.load()
        conditionTypes.store {
            oldCallback()
            subclass(C::class)
        }
    }

    /**
     * Registers a custom override condition type.
     *
     * @param C the type of the override condition to register.
     */
    inline fun <reified C : SerializableOverrideCondition> registerOverrideConditionType() {
        val oldCallback = overrideConditionTypes.load()
        overrideConditionTypes.store {
            oldCallback()
            subclass(C::class)
        }
    }

    internal fun createSerializersModule(): SerializersModule = SerializersModule {
        polymorphic(SerializableAppender::class) { appenderTypes.load()() }
        polymorphic(SerializableCondition::class) { conditionTypes.load()() }
        polymorphic(SerializableOverrideCondition::class) { overrideConditionTypes.load()() }
    }
}