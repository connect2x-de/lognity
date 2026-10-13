package de.connect2x.lognity.config

import de.connect2x.lognity.api.ExperimentalLoggingApi
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.appender.LogstashAppender
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.serialization.RefOrValue
import de.connect2x.lognity.logstash.LogstashConfig
import de.connect2x.lognity.logstash.LogstashDestination
import de.connect2x.lognity.logstash.LogstashEvent
import de.connect2x.lognity.logstash.PreferPrimaryStrategy
import de.connect2x.lognity.logstash.RandomDestinationStrategy
import de.connect2x.lognity.logstash.RoundRobinStrategy
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalLoggingApi::class)
class LogstashConfigTest {
    @BeforeTest
    fun setup() {
        Backend.set(DefaultBackend)
        SerializableConfig uses CoreConfigExtension
        SerializableConfig uses LogstashConfigExtension
    }

    private fun load(options: String): LogstashAppender {
        val source = Buffer()
        source.writeString("""{"appenders": [{"type": "logstash", $options}]}""")
        return assertIs<LogstashAppender>(SerializableConfig.load(source).appenders.single())
    }

    @Test
    fun `Defaults match the runtime configuration`() {
        val appender = load(""""destinations": ["localhost"]""")
        val actual = appender.asLogstashConfig()
        val expected = LogstashConfig(listOf(LogstashDestination("localhost")))
        assertEquals(expected.destinations, actual.destinations)
        assertEquals(expected.ringBufferSize, actual.ringBufferSize)
        assertEquals(expected.appendTimeout, actual.appendTimeout)
        assertEquals(expected.reconnectionDelay, actual.reconnectionDelay)
        assertEquals(expected.connectionTimeout, actual.connectionTimeout)
        assertEquals(expected.writeTimeout, actual.writeTimeout)
        assertEquals(expected.writeBufferSize, actual.writeBufferSize)
        assertEquals(expected.keepAliveDuration, actual.keepAliveDuration)
        assertEquals(expected.keepAliveMessage, actual.keepAliveMessage)
        assertContentEquals(expected.keepAliveBytes, actual.keepAliveBytes)
        assertEquals(expected.shutdownGracePeriod, actual.shutdownGracePeriod)
        assertEquals(expected.flushTimeout, actual.flushTimeout)
        assertNull(actual.tls)
        val strategy = assertIs<PreferPrimaryStrategy>(actual.connectionStrategy)
        assertEquals(10.seconds, strategy.minConnectionTimeBeforePrimary)
        assertNull(strategy.secondaryConnectionTTL)
        assertEquals("{{message}}", appender.pattern.resolve())
        assertEquals("default", appender.formatter.resolve())
        assertNull(appender.name.resolve())
        assertTrue(appender.filter.resolve()(Logger("test"), Level.INFO, "message", null))
    }

    @Test
    fun `Transport options are resolved without losing zero negative or nullable values`() {
        val config = load(
            """
            "destinations": ["primary:5000", " [::1]:5001 "],
            "ring_buffer_size": 16,
            "append_timeout": "-PT0.001S",
            "reconnection_delay": "PT0.1S",
            "connection_timeout": "PT0S",
            "write_timeout": "PT2S",
            "write_buffer_size": 0,
            "keep_alive_duration": "PT3S",
            "keep_alive_message": "ping\r\n",
            "keep_alive_bytes": [0, 10, -1],
            "shutdown_grace_period": "PT0S",
            "flush_timeout": "PT4S"
            """.trimIndent()
        ).asLogstashConfig()
        assertEquals(
            listOf(LogstashDestination("primary", 5000), LogstashDestination("::1", 5001)),
            config.destinations
        )
        assertEquals(16, config.ringBufferSize)
        assertEquals((-1).milliseconds, config.appendTimeout)
        assertEquals(100.milliseconds, config.reconnectionDelay)
        assertEquals(Duration.ZERO, config.connectionTimeout)
        assertEquals(2.seconds, config.writeTimeout)
        assertEquals(0, config.writeBufferSize)
        assertEquals(3.seconds, config.keepAliveDuration)
        assertEquals("ping\r\n", config.keepAliveMessage)
        assertContentEquals(byteArrayOf(0, 10, -1), config.keepAliveBytes)
        assertEquals(Duration.ZERO, config.shutdownGracePeriod)
        assertEquals(4.seconds, config.flushTimeout)
        val nullable = load(""""destinations": ["host"], "keep_alive_duration": null, "keep_alive_bytes": null""")
        assertNull(nullable.asLogstashConfig().keepAliveDuration)
        assertNull(nullable.asLogstashConfig().keepAliveBytes)
    }

    @Test
    fun `All built in strategies retain their options`() {
        fun strategy(value: String) =
            load(""""destinations": ["host"], "connection_strategy": $value""").asLogstashConfig().connectionStrategy

        val primary = assertIs<PreferPrimaryStrategy>(
            strategy(
                """{"type": "prefer_primary", "min_connection_time_before_primary": "PT2S", "secondary_connection_ttl": "PT3S"}"""
            )
        )
        assertEquals(2.seconds, primary.minConnectionTimeBeforePrimary)
        assertNull(primary.connectionTTL(0))
        assertEquals(3.seconds, primary.connectionTTL(1))
        val roundRobin = assertIs<RoundRobinStrategy>(strategy("""{"type": "round_robin", "connection_ttl": "PT4S"}"""))
        assertEquals(4.seconds, roundRobin.connectionTTL)
        val random = assertIs<RandomDestinationStrategy>(strategy("""{"type": "random", "connection_ttl": "PT5S"}"""))
        assertEquals(5.seconds, random.connectionTTL)
        assertNull(strategy("""{"type": "round_robin"}""").connectionTTL(0))
        assertNull(strategy("""{"type": "random", "connection_ttl": null}""").connectionTTL(0))
    }

    @Test
    fun `Providers and contextual templates resolve in nested options`() {
        SerializableConfig uses ConfigExtension {
            registerProvider("stash.host") { "localhost" }
            registerProvider("stash.port") { 6000 }
            registerProvider("stash.buffer") { 32 }
            registerProvider("stash.timeout") { 2.seconds }
            registerProvider("stash.service") { JsonPrimitive("test-service") }
            registerProvider("stash.destinations") { listOf(RefOrValue.Value("localhost:6001")) }
            registerTemplateProvider("stash") { name ->
                assertEquals("ttl", name)
                assertEquals(Level.DEBUG, level.resolve())
                3.seconds
            }
        }
        val source = Buffer()
        source.writeString(
            """
            {
              "level": "DEBUG",
              "appenders": [{
                "type": "logstash",
                "destinations": ["{stash.host}:{stash.port}"],
                "ring_buffer_size": "{stash.buffer}",
                "connection_timeout": "{stash.timeout}",
                "connection_strategy": {"type": "round_robin", "connection_ttl": "{stash:ttl}"},
                "encoder": {"custom_fields": {"service": "{stash.service}"}}
              }]
            }
            """.trimIndent()
        )
        val appender = assertIs<LogstashAppender>(SerializableConfig.load(source).appenders.single())
        val config = appender.asLogstashConfig()
        assertEquals(listOf(LogstashDestination("localhost", 6000)), config.destinations)
        assertEquals(32, config.ringBufferSize)
        assertEquals(2.seconds, config.connectionTimeout)
        assertEquals(3.seconds, config.connectionStrategy.connectionTTL(0))
        assertEquals("test-service", encoded(appender).getValue("service").jsonPrimitive.content)
        assertEquals(
            listOf(LogstashDestination("localhost", 6001)),
            load(""""destinations": "{stash.destinations}"""").asLogstashConfig().destinations
        )
    }

    private fun encoded(appender: LogstashAppender) = Json.parseToJsonElement(
        appender.encoder.resolveTemplate(appender.config).asEncoder(appender.config).encode(
            LogstashEvent(Instant.fromEpochMilliseconds(0), Logger("test"), "test", Level.INFO, "ä 🌍", "tag")
        ).decodeToString().trimEnd()
    ).jsonObject

    @Test
    fun `Json encoder preserves structured fields renaming omission and framing`() {
        val appender = load(
            """
            "destinations": ["host"],
            "encoder": {
              "custom_fields": {"service": "test", "count": 3, "metadata": {"active": true}, "values": [1, null]},
              "field_names": {"message": "text", "@version": "[ignore]"},
              "line_separator": "\r\n"
            }
            """.trimIndent()
        )
        val fields = encoded(appender)
        assertEquals("ä 🌍", fields.getValue("text").jsonPrimitive.content)
        assertFalse("message" in fields)
        assertFalse("@version" in fields)
        assertEquals(JsonPrimitive(3), fields["count"])
        assertEquals(Json.parseToJsonElement("""{"active":true}"""), fields["metadata"])
        assertEquals(Json.parseToJsonElement("[1,null]"), fields["values"])
        val bytes = appender.encoder.resolve().asEncoder(appender.config).encode(
            LogstashEvent(Instant.fromEpochMilliseconds(0), Logger("test"), null, Level.INFO, "message", null)
        )
        assertTrue(bytes.decodeToString().endsWith("\r\n"))
    }

    @OptIn(InternalConfigApi::class)
    @Test
    fun `Serializable appender round trips including nested strategies`() {
        val original = load(
            """"destinations": ["host:5000"], "append_timeout": "PT0.25S",
                "connection_strategy": {"type": "round_robin", "connection_ttl": "PT10S"},
                "encoder": {"custom_fields": {"service": "test"}}"""
        )
        val json = Json {
            encodeDefaults = true
            serializersModule = SerializableConfig.serializersModule
        }
        val restored = json.decodeFromString<LogstashAppender>(json.encodeToString(original))
        SerializableConfig(appenders = listOf(restored))
        assertEquals(original, restored)
        assertEquals(250.milliseconds, restored.asLogstashConfig().appendTimeout)
        assertEquals(10.seconds, restored.asLogstashConfig().connectionStrategy.connectionTTL(0))
    }

    @Test
    fun `Invalid destinations and transport settings fail before starting an appender`() {
        val options = listOf(
            "\"destinations\": []",
            "\"destinations\": [\"\"]",
            "\"destinations\": [\"host:65536\"]",
            "\"destinations\": [\"::1\"]",
            "\"ring_buffer_size\": 0",
            "\"ring_buffer_size\": 3",
            "\"write_buffer_size\": -1",
            "\"reconnection_delay\": \"PT0S\"",
            "\"connection_timeout\": \"-PT1S\"",
            "\"write_timeout\": \"-PT1S\"",
            "\"keep_alive_duration\": \"PT0S\"",
            "\"shutdown_grace_period\": \"-PT1S\"",
            "\"flush_timeout\": \"PT0S\"",
            "\"connection_strategy\": {\"type\": \"prefer_primary\", \"min_connection_time_before_primary\": \"-PT1S\"}",
            "\"connection_strategy\": {\"type\": \"prefer_primary\", \"secondary_connection_ttl\": \"PT0S\"}",
            "\"connection_strategy\": {\"type\": \"round_robin\", \"connection_ttl\": \"PT0S\"}",
            "\"connection_strategy\": {\"type\": \"random\", \"connection_ttl\": \"-PT1S\"}"
        )
        for (option in options) {
            val properties =
                if (option.startsWith("\"destinations\"")) option else "\"destinations\": [\"host\"], $option"
            assertFailsWith<IllegalArgumentException>(option) { load(properties).config.asConfig() }
        }
    }

    @Test
    fun `Missing destinations unknown strategies and malformed durations are rejected`() {
        assertFailsWith<SerializationException> { load(""""name": "missing"""") }
        assertFailsWith<SerializationException> {
            load(""""destinations": ["host"], "connection_strategy": {"type": "unknown"}""")
        }
        assertFailsWith<IllegalArgumentException> {
            load(""""destinations": ["host"], "connection_timeout": "not-a-duration"""")
        }
        assertFailsWith<IllegalStateException> {
            load(""""destinations": ["{missing.logstash.host}"]""").asLogstashConfig()
        }
    }

    @Test
    fun `Tls preserves runtime platform validation`() {
        val supported = try {
            LogstashConfig(listOf(LogstashDestination("host")), tls = {})
            true
        } catch (_: IllegalArgumentException) {
            false
        }
        val appender = load(""""destinations": ["host"], "tls": true""")
        if (supported) assertTrue(appender.asLogstashConfig().tls != null)
        else assertFailsWith<IllegalArgumentException> { appender.asLogstashConfig() }
    }
}