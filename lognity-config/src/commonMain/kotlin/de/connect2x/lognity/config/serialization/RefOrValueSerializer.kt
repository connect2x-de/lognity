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
        const val REFERENCE_BEGIN: Char = '{'
        const val REFERENCE_END: Char = '}'
    }

    override val descriptor: SerialDescriptor = buildSerialDescriptor("RefOrValue", PolymorphicKind.OPEN)

    override fun serialize(encoder: Encoder, value: RefOrValue<T>) {
        when (value) {
            is RefOrValue.Ref -> encoder.encodeString("$REFERENCE_BEGIN${value.name}$REFERENCE_END")
            is RefOrValue.Value -> encoder.encodeSerializableValue(valueSerializer, value.value)
            is RefOrValue.LerpedString -> { // This may encode mixed segments of values and refs
                val buffer = StringBuilder()
                for (segment in value.segments) buffer.append(
                    when (segment) {
                        is RefOrValue.Ref -> "$REFERENCE_BEGIN${segment.name}$REFERENCE_END"
                        else -> segment.resolve().toString()
                    }
                )
                encoder.encodeString(buffer.toString())
            }
        }
    }

    // @formatter:off
    private fun isBareReference(s: String): Boolean = s.startsWith(REFERENCE_BEGIN)
        && s.endsWith(REFERENCE_END)
        && s.getOrNull(1) != REFERENCE_BEGIN
        && s.getOrNull(s.length - 2) != REFERENCE_END

    private fun stripReferenceDelimiters(s: String): String = s.substringAfter(REFERENCE_BEGIN)
        .substringBeforeLast(REFERENCE_END)
    // @formatter:on

    private fun containsReferences(s: String): Boolean {
        var count = 0
        var isReference = false
        for (index in s.indices) {
            val c = s[index]
            fun lookahead(): Char? = s.getOrNull(index + 1)
            fun lookback(): Char? = s.getOrNull(index - 1)
            if (isReference && c == REFERENCE_END && lookahead() != REFERENCE_END && lookback() != REFERENCE_END) {
                isReference = false
                continue
            }
            if (c == REFERENCE_BEGIN && lookahead() != REFERENCE_BEGIN && lookback() != REFERENCE_BEGIN) {
                isReference = true
                count++
            }
        }
        return count > 0
    }

    private fun parseLerpedString(s: String): RefOrValue.LerpedString {
        // At this point, we can assume this is a lerped string
        val segments = ArrayList<RefOrValue<*>>()
        val buffer = StringBuilder()
        var isReference = false
        for (index in s.indices) {
            val c = s[index]
            fun lookahead(): Char? = s.getOrNull(index + 1)
            fun lookback(): Char? = s.getOrNull(index - 1)
            if (isReference && c == REFERENCE_END && lookahead() != REFERENCE_END && lookback() != REFERENCE_END) {
                // We flush all last content as reference identifier
                if (buffer.isNotEmpty()) segments += RefOrValue.Ref<Any>(buffer.toString())
                buffer.clear()
                isReference = false
                continue
            }
            if (c == REFERENCE_BEGIN && lookahead() != REFERENCE_BEGIN && lookback() != REFERENCE_BEGIN) {
                // We flush all last content as literal
                if (buffer.isNotEmpty()) segments += RefOrValue.Value(buffer.toString())
                buffer.clear()
                isReference = true
                continue
            }
            buffer.append(c)
        }
        // Flush any remaining segment
        if (isReference && buffer.isNotEmpty()) segments += RefOrValue.Ref<Any>(buffer.toString())
        else if (buffer.isNotEmpty()) segments += RefOrValue.Value(buffer.toString())
        return RefOrValue.LerpedString(segments)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): RefOrValue<T> {
        require(decoder is JsonDecoder) { "RefOrValueSerializer only supports JSON" }
        val element = decoder.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.isString -> {
                val content = element.content
                when {
                    isBareReference(content) -> RefOrValue.Ref(stripReferenceDelimiters(content))
                    containsReferences(content) -> parseLerpedString(content) as RefOrValue<T>
                    else -> RefOrValue.Value(decoder.json.decodeFromJsonElement(valueSerializer, element))
                }
            }

            else -> RefOrValue.Value(decoder.json.decodeFromJsonElement(valueSerializer, element))
        }
    }
}