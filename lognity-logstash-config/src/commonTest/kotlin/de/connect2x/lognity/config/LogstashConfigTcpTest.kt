@file:OptIn(de.connect2x.lognity.api.ExperimentalLoggingApi::class)

package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.logstash.LogstashAppender
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.readLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class LogstashConfigTcpTest {
    @BeforeTest
    fun setup() {
        Backend.set(DefaultBackend)
        SerializableConfig uses CoreConfigExtension
        SerializableConfig uses LogstashConfigExtension
    }

    @Test
    fun `Registered factory delivers filtered JSON lines and closes the connection`() = runBlocking {
        val selector = SelectorManager(Dispatchers.IO)
        val server = aSocket(selector).tcp().bind("127.0.0.1", 0)
        var appender: LogstashAppender? = null
        try {
            withTimeout(10.seconds) {
                val source = Buffer()
                source.writeString(
                    """
                    {
                      "conditions": [{"type": "message", "name": "accepted", "condition": "NOT_CONTAINS", "value": "excluded"}],
                      "appenders": [{
                        "type": "logstash",
                        "destinations": ["127.0.0.1:${(server.localAddress as InetSocketAddress).port}"],
                        "name": "remote",
                        "pattern": "{{level}} {{message}}",
                        "formatter": "default",
                        "filter": {"conditions": ["{conditions:accepted}"]},
                        "shutdown_grace_period": "PT2S",
                        "flush_timeout": "PT3S",
                        "encoder": {"custom_fields": {"service": "config-test"}, "field_names": {"message": "text"}}
                      }]
                    }
                    """.trimIndent()
                )
                val serializable = SerializableConfig.load(source)
                val runtime = serializable.asConfig()
                val remote = assertIs<LogstashAppender>(runtime.appenders.single())
                appender = remote
                assertSame(runtime, serializable.asConfig())
                assertEquals("remote", remote.name)
                assertEquals("{{level}} {{message}}", remote.pattern)
                assertSame(Backend.defaultFormatter, remote.formatter)
                assertTrue(remote.isStarted)
                val received = async(Dispatchers.IO) {
                    val client = server.accept()
                    try {
                        val input = client.openReadChannel()
                        val line = input.readLine()!!
                        assertNull(input.readLine())
                        Json.parseToJsonElement(line).jsonObject
                    }
                    finally {
                        client.close()
                    }
                }
                val logger = Logger("config.test")
                remote.append(logger, Level.INFO, "excluded", null)
                remote.append(logger, Level.INFO, "ä 🌍", null)
                remote.flushSuspend()
                remote.stopSuspend()
                assertFalse(remote.isStarted)
                val event = received.await()
                assertEquals("ä 🌍", event.getValue("text").jsonPrimitive.content)
                assertEquals("config.test", event.getValue("logger_name").jsonPrimitive.content)
                assertEquals("config-test", event.getValue("service").jsonPrimitive.content)
                assertEquals("INFO", event.getValue("level").jsonPrimitive.content)
            }
        }
        finally {
            appender?.stopSuspend()
            server.close()
            selector.close()
        }
    }
}