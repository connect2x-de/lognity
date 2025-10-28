package net.folivo.lognity.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.folivo.lognity.api.logger.Context

@Polymorphic
@Serializable
sealed interface SerializableValue<T : Any> {
    companion object {
        val serializersModule: SerializersModule = SerializersModule {
            polymorphic(SerializableValue::class) {
                subclass(StringValue::class)
                subclass(StringListValue::class)
                subclass(BooleanValue::class)
                subclass(BooleanListValue::class)
                subclass(ByteValue::class)
                subclass(ByteListValue::class)
                subclass(ShortValue::class)
                subclass(ShortListValue::class)
                subclass(IntValue::class)
                subclass(IntListValue::class)
                subclass(LongValue::class)
                subclass(LongListValue::class)
                subclass(FloatValue::class)
                subclass(FloatListValue::class)
                subclass(DoubleValue::class)
                subclass(DoubleListValue::class)
                subclass(CharValue::class)
                subclass(CharListValue::class)
            }
        }
    }

    val value: T

    fun createKey(name: String): Context.Key<T>

    @SerialName("string")
    @Serializable
    data class StringValue(override val value: String) : SerializableValue<String> {
        override fun createKey(name: String): Context.Key<String> = Context.Key.create(name)
    }

    @SerialName("string_list")
    @Serializable
    data class StringListValue(override val value: List<String>) : SerializableValue<List<String>> {
        override fun createKey(name: String): Context.Key<List<String>> = Context.Key.create(name)
    }

    @SerialName("boolean")
    @Serializable
    data class BooleanValue(override val value: Boolean) : SerializableValue<Boolean> {
        override fun createKey(name: String): Context.Key<Boolean> = Context.Key.create(name)
    }

    @SerialName("boolean_list")
    @Serializable
    data class BooleanListValue(override val value: List<Boolean>) : SerializableValue<List<Boolean>> {
        override fun createKey(name: String): Context.Key<List<Boolean>> = Context.Key.create(name)
    }

    @SerialName("byte")
    @Serializable
    data class ByteValue(override val value: Byte) : SerializableValue<Byte> {
        override fun createKey(name: String): Context.Key<Byte> = Context.Key.create(name)
    }

    @SerialName("byte_list")
    @Serializable
    data class ByteListValue(override val value: List<Byte>) : SerializableValue<List<Byte>> {
        override fun createKey(name: String): Context.Key<List<Byte>> = Context.Key.create(name)
    }

    @SerialName("short")
    @Serializable
    data class ShortValue(override val value: Short) : SerializableValue<Short> {
        override fun createKey(name: String): Context.Key<Short> = Context.Key.create(name)
    }

    @SerialName("short_list")
    @Serializable
    data class ShortListValue(override val value: List<Short>) : SerializableValue<List<Short>> {
        override fun createKey(name: String): Context.Key<List<Short>> = Context.Key.create(name)
    }

    @SerialName("int")
    @Serializable
    data class IntValue(override val value: Int) : SerializableValue<Int> {
        override fun createKey(name: String): Context.Key<Int> = Context.Key.create(name)
    }

    @SerialName("int_list")
    @Serializable
    data class IntListValue(override val value: List<Int>) : SerializableValue<List<Int>> {
        override fun createKey(name: String): Context.Key<List<Int>> = Context.Key.create(name)
    }

    @SerialName("long")
    @Serializable
    data class LongValue(override val value: Long) : SerializableValue<Long> {
        override fun createKey(name: String): Context.Key<Long> = Context.Key.create(name)
    }

    @SerialName("long_list")
    @Serializable
    data class LongListValue(override val value: List<Long>) : SerializableValue<List<Long>> {
        override fun createKey(name: String): Context.Key<List<Long>> = Context.Key.create(name)
    }

    @SerialName("float")
    @Serializable
    data class FloatValue(override val value: Float) : SerializableValue<Float> {
        override fun createKey(name: String): Context.Key<Float> = Context.Key.create(name)
    }

    @SerialName("float_list")
    @Serializable
    data class FloatListValue(override val value: List<Float>) : SerializableValue<List<Float>> {
        override fun createKey(name: String): Context.Key<List<Float>> = Context.Key.create(name)
    }

    @SerialName("double")
    @Serializable
    data class DoubleValue(override val value: Double) : SerializableValue<Double> {
        override fun createKey(name: String): Context.Key<Double> = Context.Key.create(name)
    }

    @SerialName("double_list")
    @Serializable
    data class DoubleListValue(override val value: List<Double>) : SerializableValue<List<Double>> {
        override fun createKey(name: String): Context.Key<List<Double>> = Context.Key.create(name)
    }

    @SerialName("char")
    @Serializable
    data class CharValue(override val value: Char) : SerializableValue<Char> {
        override fun createKey(name: String): Context.Key<Char> = Context.Key.create(name)
    }

    @SerialName("char_list")
    @Serializable
    data class CharListValue(override val value: List<Char>) : SerializableValue<List<Char>> {
        override fun createKey(name: String): Context.Key<List<Char>> = Context.Key.create(name)
    }
}