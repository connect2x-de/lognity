package de.connect2x.lognity.config.logstash

import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import de.connect2x.lognity.logstash.LogstashJsonEncoder as LogstashJsonEncoderImpl

/** JSON Lines encoder options. A field name of `[ignore]` omits that field. */
@Serializable
data class LogstashJsonEncoder(
    @SerialName("custom_fields") val customFields: RefOrValue<Map<String, RefOrValue<JsonElement>>> = RefOrValue.Value(
        emptyMap()
    ),
    @SerialName("field_names") val fieldNames: RefOrValue<Map<String, RefOrValue<String>>> = RefOrValue.Value(emptyMap()),
    @SerialName("line_separator") val lineSeparator: RefOrValue<String> = RefOrValue.Value("\n")
) {
    internal fun asEncoder(config: SerializableConfig): LogstashJsonEncoderImpl = LogstashJsonEncoderImpl(
        customFields = customFields.resolveTemplate(config).mapValues { (_, value) -> value.resolveTemplate(config) },
        fieldNames = fieldNames.resolveTemplate(config).mapValues { (_, value) -> value.resolveTemplate(config) },
        lineSeparator = lineSeparator.resolveTemplate(config)
    )
}