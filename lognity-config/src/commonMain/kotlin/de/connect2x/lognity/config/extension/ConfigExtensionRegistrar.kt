package de.connect2x.lognity.config.extension

import co.touchlab.stately.collections.SharedHashMap
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.config.SerializableConfigDsl
import de.connect2x.lognity.config.appender.AppenderFactory
import de.connect2x.lognity.config.appender.SerializableAppender
import de.connect2x.lognity.config.condition.SerializableCondition
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass

/**
 * Registrar for custom configuration extensions.
 *
 * This class provides methods to register custom appender and condition types,
 * allowing them to be used in the polymorphic serialization of the configuration.
 */
@OptIn(ExperimentalAtomicApi::class)
@SerializableConfigDsl
class ConfigExtensionRegistrar internal constructor() {
    @PublishedApi
    internal var appenderTypes: AtomicReference<PolymorphicModuleBuilder<SerializableAppender>.() -> Unit> =
        AtomicReference {}

    @PublishedApi
    internal val appenderFactories: SharedHashMap<KClass<out SerializableAppender>, AppenderFactory<SerializableAppender>> =
        SharedHashMap()

    @PublishedApi
    internal var conditionTypes: AtomicReference<PolymorphicModuleBuilder<SerializableCondition>.() -> Unit> =
        AtomicReference {}

    internal val formatterTypes: SharedHashMap<String, Formatter> = SharedHashMap()

    fun registerFormatterType(name: String, formatter: Formatter) {
        require(name !in formatterTypes) { "Formatter type '$name' already exists" }
        formatterTypes[name] = formatter
    }

    /**
     * Registers a custom appender type and its corresponding factory.
     *
     * @param A the type of the serializable appender to register.
     * @param factory the factory responsible for creating the appender instance.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified A : SerializableAppender> registerAppenderType(noinline factory: AppenderFactory<A>) {
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

    internal fun createSerializersModule(): SerializersModule = SerializersModule {
        polymorphic(SerializableAppender::class) { appenderTypes.load()() }
        polymorphic(SerializableCondition::class) { conditionTypes.load()() }
    }
}