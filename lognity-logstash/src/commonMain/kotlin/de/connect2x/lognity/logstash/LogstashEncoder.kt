package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant

/** Event data is captured on the producer, before asynchronous encoding. */
@ExperimentalLoggingApi
data class LogstashEvent(
    val timestamp: Instant,
    val logger: Logger,
    val loggerName: String?,
    val level: Level,
    val message: String,
    val markerName: String?
)

/** Encoders own framing, charset, and optional per-connection headers and footers. */
@ExperimentalLoggingApi
fun interface LogstashEncoder {
    fun encode(event: LogstashEvent): ByteArray
    fun header(): ByteArray = byteArrayOf()
    fun footer(): ByteArray = byteArrayOf()
}

/** UTF-8 JSON Lines, with configurable fields and additional per-event JSON providers. */
@ExperimentalLoggingApi
class LogstashJsonEncoder(
    customFields: Map<String, JsonElement> = emptyMap(),
    fieldNames: Map<String, String> = emptyMap(),
    private val lineSeparator: String = "\n",
    private val additionalFields: (LogstashEvent) -> Map<String, JsonElement> = { emptyMap() }
) : LogstashEncoder {
    private val customFields = customFields.toMap()
    private val fieldNames = fieldNames.toMap()

    override fun encode(event: LogstashEvent): ByteArray {
        val fields = buildJsonObject {
            put("@timestamp", event.timestamp.toString())
            put("@version", "1")
            put("message", event.message)
            event.loggerName?.let { put("logger_name", it) }
            put("level", event.level.name)
            put("level_value", if (event.level == Level.TRACE) 5000 else event.level.ordinal * 10000)
            event.markerName?.let { name -> put("tags", buildJsonArray { add(name) }) }
            customFields.forEach { (name, value) -> put(name, value) }
            additionalFields(event).forEach { (name, value) -> put(name, value) }
        }
        val renamed = fields.mapNotNull { (name, value) ->
            val fieldName = fieldNames[name] ?: name
            if (fieldName == "[ignore]") null else fieldName to value
        }.toMap()
        return (JsonObject(renamed).toString() + lineSeparator).encodeToByteArray()
    }
}