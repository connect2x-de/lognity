package de.connect2x.lognity.config.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class RefOrValueSerializer<T>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<RefOrValue<T>> {
    companion object {
        const val REFERENCE_PREFIX: String = "$$"
    }

    override val descriptor: SerialDescriptor = buildSerialDescriptor("RefOrValue", PolymorphicKind.OPEN)

    override fun serialize(encoder: Encoder, value: RefOrValue<T>) {
        when (value) {
            is RefOrValue.Ref -> encoder.encodeString("$REFERENCE_PREFIX${value.name}")
            is RefOrValue.Value -> encoder.encodeSerializableValue(valueSerializer, value.value)
        }
    }

    override fun deserialize(decoder: Decoder): RefOrValue<T> {
        require(decoder is JsonDecoder) { "RefOrValueSerializer only supports JSON" }
        val element = decoder.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.isString -> {
                val content = element.content
                if (content.startsWith(REFERENCE_PREFIX)) RefOrValue.Ref(content.substringAfter(REFERENCE_PREFIX))
                else RefOrValue.Value(decoder.json.decodeFromJsonElement(valueSerializer, element))
            }

            else -> RefOrValue.Value(decoder.json.decodeFromJsonElement(valueSerializer, element))
        }
    }
}