package de.connect2x.lognity.config.extension

import de.connect2x.lognity.config.SerializableConfigDsl
import de.connect2x.lognity.config.appender.AppenderFactory
import de.connect2x.lognity.config.appender.SerializableAppender
import de.connect2x.lognity.config.condition.SerializableCondition
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
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
    internal var appenderTypes: PolymorphicModuleBuilder<SerializableAppender>.() -> Unit = {}

    @PublishedApi
    internal val appenderFactories: HashMap<KClass<out SerializableAppender>, AppenderFactory<SerializableAppender>> =
        HashMap()

    @PublishedApi
    internal var conditionTypes: PolymorphicModuleBuilder<SerializableCondition>.() -> Unit = {}

    /**
     * Registers a custom appender type and its corresponding factory.
     *
     * @param A the type of the serializable appender to register.
     * @param factory the factory responsible for creating the appender instance.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified A : SerializableAppender> registerAppenderType(noinline factory: AppenderFactory<A>) {
        val oldCallback = appenderTypes
        appenderTypes = {
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
        val oldCallback = conditionTypes
        conditionTypes = {
            oldCallback()
            subclass(C::class)
        }
    }

    internal fun createSerializersModule(): SerializersModule = SerializersModule {
        polymorphic(SerializableAppender::class) { appenderTypes() }
        polymorphic(SerializableCondition::class) { conditionTypes() }
    }
}