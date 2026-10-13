package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.logger.Level
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalLoggingApi::class)
class LogstashEncoderTest {
    private val event = LogstashEvent(
        Instant.parse("2026-10-09T12:00:00Z"),
        TestLogger(),
        "test.logger",
        Level.INFO,
        "Quotes \" slash \\ newline\n Unicode ä 🌍",
        "security"
    )

    @Test
    fun jsonLinesPreserveMessageAndMetadata() {
        val encoded = LogstashJsonEncoder().encode(event).decodeToString()
        assertTrue(encoded.endsWith('\n'))
        assertEquals(1, encoded.count { it == '\n' })
        val json = Json.parseToJsonElement(encoded).jsonObject
        assertEquals(event.message, json.getValue("message").jsonPrimitive.content)
        assertEquals(event.timestamp.toString(), json.getValue("@timestamp").jsonPrimitive.content)
        assertEquals("1", json.getValue("@version").jsonPrimitive.content)
        assertEquals("test.logger", json.getValue("logger_name").jsonPrimitive.content)
        assertEquals("INFO", json.getValue("level").jsonPrimitive.content)
        assertEquals(20000, json.getValue("level_value").jsonPrimitive.int)
        assertEquals("security", json.getValue("tags").jsonArray.single().jsonPrimitive.content)
    }

    @Test
    fun allLevelsHaveLogbackCompatibleNumbers() {
        listOf(5000, 10000, 20000, 30000, 40000, 50000).forEachIndexed { index, number ->
            val encoded = LogstashJsonEncoder().encode(event.copy(level = Level.entries[index]))
            assertEquals(
                number,
                Json.parseToJsonElement(encoded.decodeToString()).jsonObject.getValue("level_value").jsonPrimitive.int
            )
        }
    }

    @Test
    fun customFieldsProvidersRenamingAndSuppressionAreSupported() {
        val encoder = LogstashJsonEncoder(
            customFields = mapOf("service" to JsonPrimitive("test")),
            fieldNames = mapOf("message" to "log", "@version" to "[ignore]"),
            lineSeparator = "\r\n",
            additionalFields = { mapOf("answer" to JsonPrimitive(42)) })
        val encoded = encoder.encode(event.copy(loggerName = null, markerName = null)).decodeToString()
        assertTrue(encoded.endsWith("\r\n"))
        val json = Json.parseToJsonElement(encoded).jsonObject
        assertEquals(event.message, json.getValue("log").jsonPrimitive.content)
        assertEquals("test", json.getValue("service").jsonPrimitive.content)
        assertEquals(42, json.getValue("answer").jsonPrimitive.int)
        listOf("message", "@version", "logger_name", "tags").forEach { assertFalse(it in json) }
    }
}