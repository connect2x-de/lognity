@file:OptIn(de.connect2x.lognity.api.ExperimentalLoggingApi::class)

package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.logger.Level
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class LogstashTcpTest {
    @Test
    fun shutdownDrainsLargePayloadBeforeClosingSocket() = runBlocking {
        val selector = SelectorManager(Dispatchers.IO)
        val server = aSocket(selector).tcp().bind("127.0.0.1", 0)
        val payload = "ä".repeat(512 * 1024)
        val appender = LogstashAppender(
            LogstashConfig(
            destinations = listOf(
                LogstashDestination(
                    "127.0.0.1", (server.localAddress as InetSocketAddress).port
                )
            ), writeTimeout = 5.seconds, shutdownGracePeriod = 5.seconds, socketOptions = { sendBufferSize = 1024 }),
            LogstashEncoder { (it.message + "\n").encodeToByteArray() })
        try {
            withTimeout(15.seconds) {
                val received = async(Dispatchers.IO) {
                    val client = server.accept()
                    try {
                        delay(100)
                        val input = client.openReadChannel()
                        assertEquals(payload, input.readLineStrict(limit = 2 * 1024 * 1024))
                        assertEquals(null, input.readLine())
                    }
                    finally {
                        client.close()
                    }
                }
                appender.append(TestLogger(), Level.INFO, payload, null)
                appender.stopSuspend()
                received.await()
            }
        }
        finally {
            appender.stopSuspend()
            server.close()
            selector.close()
        }
    }

    @Test
    fun realTcpReceivesUnicodeJsonLinesAndEofOnShutdown() = runBlocking {
        val selector = SelectorManager(Dispatchers.IO)
        val server = aSocket(selector).tcp().bind("127.0.0.1", 0)
        val appender = LogstashAppender(
            LogstashConfig(
                destinations = listOf(
                    LogstashDestination(
                        "127.0.0.1", (server.localAddress as InetSocketAddress).port
                    )
                ), shutdownGracePeriod = 2.seconds, flushTimeout = 5.seconds
            )
        )
        try {
            withTimeout(10.seconds) {
                val received = async(Dispatchers.IO) {
                    val client = server.accept()
                    try {
                        val input = client.openReadChannel()
                        val lines = List(3) { input.readLine()!! }
                        assertEquals(null, input.readLine())
                        lines
                    }
                    finally {
                        client.close()
                    }
                }
                val logger = TestLogger()
                listOf("plain", "quotes \" and\nnewlines", "ä 🌍").forEach {
                    appender.append(logger, Level.INFO, it, null)
                }
                appender.flushSuspend()
                appender.stopSuspend()
                assertEquals(listOf("plain", "quotes \" and\nnewlines", "ä 🌍"), received.await().map {
                    Json.parseToJsonElement(it).jsonObject.getValue("message").jsonPrimitive.content
                })
            }
        }
        finally {
            appender.stopSuspend()
            server.close()
            selector.close()
        }
    }

    @Test
    fun realTcpFailsOverFromRefusedConnection() = runBlocking {
        val selector = SelectorManager(Dispatchers.IO)
        val server = aSocket(selector).tcp().bind("127.0.0.1", 0)
        val unavailable = aSocket(selector).tcp().bind("127.0.0.1", 0)
        val unavailablePort = (unavailable.localAddress as InetSocketAddress).port
        unavailable.close()
        val appender = LogstashAppender(
            LogstashConfig(
                destinations = listOf(
                    LogstashDestination("127.0.0.1", unavailablePort),
                    LogstashDestination("127.0.0.1", (server.localAddress as InetSocketAddress).port)
                ),
                reconnectionDelay = 100.milliseconds,
                connectionTimeout = 1.seconds,
                shutdownGracePeriod = 1.seconds,
                flushTimeout = 5.seconds
            ), LogstashEncoder { (it.message + "\n").encodeToByteArray() })
        try {
            withTimeout(10.seconds) {
                val received = async(Dispatchers.IO) {
                    val client = server.accept()
                    try {
                        client.openReadChannel().readLine()
                    }
                    finally {
                        client.close()
                    }
                }
                appender.appendSuspend(TestLogger(), Level.INFO, "failover", null)
                appender.flushSuspend()
                assertEquals("failover", received.await())
            }
        }
        finally {
            appender.stopSuspend()
            server.close()
            selector.close()
        }
    }

    @Test
    fun nativeTlsIsRejectedBeforeAnyConnection() {
        if (!supportsLogstashTls) {
            assertFailsWith<IllegalArgumentException> {
                LogstashConfig(listOf(LogstashDestination("localhost")), tls = {})
            }
        }
    }
}