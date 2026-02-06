package de.connect2x.lognity.config.serialization

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Serializer for [RefOrValue] that handles both direct values and string references.
 *
 * References are denoted by curly braces, e.g., `{myReference}`.
 * If the value is a string, it can also contain multiple references interspersed with literals,
 * which will be parsed into a [RefOrValue.LerpedString].
 *
 * @param T the type of the value being wrapped.
 * @property valueSerializer the serializer for the underlying value type.
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class RefOrValueSerializer<T>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<RefOrValue<T>> {
    companion object {
        const val REFERENCE_BEGIN: Char = '{'
        const val REFERENCE_END: Char = '}'
        const val TEMPLATE_DELIMITER: Char = ':'
        const val TEMPLATE_PARAM_BEGIN: Char = '('
        const val TEMPLATE_PARAM_END: Char = ')'
        const val TEMPLATE_PARAM_DELIMITER: Char = ','
        const val TEMPLATE_PARAM_STR_DELIMITER: Char = '\''
        const val TEMPLATE_PARAM_BUILTIN_BEGIN: Char = '<'
        const val TEMPLATE_PARAM_BUILTIN_END: Char = '>'
        const val TEMPLATE_PARAM_BUILTIN_DELIMITER: Char = '.'
    }

    override val descriptor: SerialDescriptor = buildSerialDescriptor("RefOrValue", PolymorphicKind.OPEN)

    private fun serializeTemplateRef(encoder: Encoder, value: RefOrValue.TemplateRef<T>) {
        val parameters = value.parameters
        if (parameters.isNotEmpty()) {
            // Handle parametrized template reference
            val builder = StringBuilder()
            builder.append("$REFERENCE_BEGIN${value.prefix}$TEMPLATE_DELIMITER${value.name}$TEMPLATE_PARAM_BEGIN")
            for (parameterIndex in parameters.indices) {
                val parameter = parameters[parameterIndex]
                builder.append(parameter.resolve().toString())
                if (parameterIndex < parameters.lastIndex) {
                    builder.append(TEMPLATE_PARAM_DELIMITER)
                }
            }
            builder.append("$TEMPLATE_PARAM_END$REFERENCE_END")
            encoder.encodeString(builder.toString())
            return
        }
        encoder.encodeString("$REFERENCE_BEGIN${value.prefix}$TEMPLATE_DELIMITER${value.name}${REFERENCE_END}")
    }

    private fun serializeLerpedString(encoder: Encoder, value: RefOrValue.LerpedString) {
        val buffer = StringBuilder()
        for (segment in value.segments) buffer.append(
            when (segment) {
                is RefOrValue.Ref -> "$REFERENCE_BEGIN${segment.name}$REFERENCE_END"
                else -> segment.resolve().toString()
            }
        )
        encoder.encodeString(buffer.toString())
    }

    override fun serialize(encoder: Encoder, value: RefOrValue<T>) {
        when (value) {
            is RefOrValue.Ref -> encoder.encodeString("$REFERENCE_BEGIN${value.name}$REFERENCE_END")
            is RefOrValue.TemplateRef -> serializeTemplateRef(encoder, value)
            is RefOrValue.Value -> encoder.encodeSerializableValue(valueSerializer, value.value)
            is RefOrValue.LerpedString -> serializeLerpedString(encoder, value)
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

    private fun deserializeLerpedString(s: String): RefOrValue.LerpedString {
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
    private fun <T> deserializeTemplateParameter(s: String): RefOrValue<T> {
        return when {
            s.count { c -> c == TEMPLATE_PARAM_STR_DELIMITER } == 2 -> {
                val literalValue =
                    s.substringAfter(TEMPLATE_PARAM_STR_DELIMITER).substringBeforeLast(TEMPLATE_PARAM_STR_DELIMITER)
                RefOrValue.Value(literalValue) as RefOrValue<T>
            }

            TEMPLATE_PARAM_BUILTIN_BEGIN in s && TEMPLATE_PARAM_BUILTIN_END in s -> {
                check(s.count { c -> c == TEMPLATE_PARAM_BUILTIN_BEGIN } == 1) { "Malformed template parameter builtin: $s" }
                check(s.count { c -> c == TEMPLATE_PARAM_BUILTIN_END } == 1) { "Malformed template parameter builtin: $s" }
                check(s.indexOf(TEMPLATE_PARAM_BEGIN) < s.indexOf(TEMPLATE_PARAM_END)) { "Malformed template parameter builtin: $s" }
                val refString =
                    s.substringAfter(TEMPLATE_PARAM_BUILTIN_BEGIN).substringBeforeLast(TEMPLATE_PARAM_BUILTIN_END)
                val (prefix, name) = refString.split(TEMPLATE_PARAM_BUILTIN_DELIMITER)
                // Builtins for template arguments must be constant expressions, so we can resolve them while deserializing
                val value = requireNotNull(SerializableConfig.extensionRegistrar.findBuiltinProvider<Any>(prefix)) {
                    "Could not find builtin provider for value '$refString'"
                }(name)
                RefOrValue.Value(value) as RefOrValue<T>
            }

            else -> error("Malformed template parameter argument: $s")
        }
    }

    private fun deserializeTemplateRef(s: String): RefOrValue.TemplateRef<T> {
        if (TEMPLATE_PARAM_BEGIN in s && TEMPLATE_PARAM_END in s) {
            // Handle parametrized template provider
            check(s.count { c -> c == TEMPLATE_PARAM_BEGIN } == 1) { "Malformed template reference: $s" }
            check(s.count { c -> c == TEMPLATE_PARAM_END } == 1) { "Malformed template reference: $s" }
            check(s.indexOf(TEMPLATE_PARAM_BEGIN) < s.indexOf(TEMPLATE_PARAM_END)) { "Malformed template reference: $s" }
            val delimiterIndex = s.indexOf(TEMPLATE_DELIMITER)
            val prefix = s.substring(1..<delimiterIndex)
            val paramBeginIndex = s.indexOf(TEMPLATE_PARAM_BEGIN)
            val paramEndIndex = s.indexOf(TEMPLATE_PARAM_END)
            val name = s.substring(delimiterIndex + 1..<paramBeginIndex)
            val paramString = s.substring(paramBeginIndex + 1..<paramEndIndex).trim()
            // @formatter:off
            val parameters = paramString.split(TEMPLATE_PARAM_DELIMITER)
                .map { param -> deserializeTemplateParameter<Any>(param.trim()) }
            // @formatter:on
            return RefOrValue.TemplateRef(prefix, name, parameters)
        }
        val (prefix, name) = stripReferenceDelimiters(s).split(TEMPLATE_DELIMITER)
        return RefOrValue.TemplateRef(prefix, name)
    }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeString(s: String, element: JsonElement, decoder: JsonDecoder): RefOrValue<T> {
        return when {
            isBareReference(s) -> when {
                TEMPLATE_DELIMITER in s -> deserializeTemplateRef(s)
                else -> RefOrValue.Ref(stripReferenceDelimiters(s))
            }

            containsReferences(s) -> deserializeLerpedString(s) as RefOrValue<T>
            else -> RefOrValue.Value(decoder.json.decodeFromJsonElement(valueSerializer, element))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): RefOrValue<T> {
        require(decoder is JsonDecoder) { "RefOrValueSerializer only supports JSON" }
        val element = decoder.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.isString -> deserializeString(element.content, element, decoder)
            else -> RefOrValue.Value(decoder.json.decodeFromJsonElement(valueSerializer, element))
        }
    }
}