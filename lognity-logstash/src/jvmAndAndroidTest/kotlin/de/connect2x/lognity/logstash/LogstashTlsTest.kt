@file:OptIn(de.connect2x.lognity.api.ExperimentalLoggingApi::class)

package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.logger.Level
import io.ktor.network.tls.addKeyStore
import io.ktor.network.tls.certificates.buildKeyStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.InetAddress
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class LogstashTlsTest {
    private val password = "test-password"

    @Test
    fun tlsDeliversToServerWithExplicitTrustAndClientCertificate() = runBlocking {
        val keyStore = keyStore()
        val trust = trustManager(keyStore)
        val server = server(keyStore).apply { needClientAuth = true }
        val appender = LogstashAppender(config(server).copy(tls = {
            trustManager = trust
            addKeyStore(keyStore, password.toCharArray())
        }), LogstashEncoder { (it.message + "\n").encodeToByteArray() })
        try {
            withTimeout(15.seconds) {
                val received = async(Dispatchers.IO) {
                    server.accept().use { client ->
                        client.soTimeout = 5000
                        val tlsClient = client as SSLSocket
                        tlsClient.startHandshake()
                        assertTrue(tlsClient.session.peerCertificates.isNotEmpty())
                        client.getInputStream().bufferedReader().readLine()
                    }
                }
                appender.append(TestLogger(), Level.INFO, "encrypted ä", null)
                appender.flushSuspend()
                assertEquals("encrypted ä", received.await())
            }
        } finally {
            appender.stopSuspend()
            server.close()
        }
    }

    @Test
    fun defaultTrustRejectsSelfSignedCertificate() = runBlocking {
        val server = server(keyStore())
        val failed = CompletableDeferred<Throwable>()
        val appender = LogstashAppender(config(server).copy(
            tls = {},
            listeners = listOf(object : LogstashListener {
                override fun connectionFailed(destination: LogstashDestination, cause: Throwable) { failed.complete(cause) }
            })
        ))
        try {
            withTimeout(10.seconds) {
                val handshake = async(Dispatchers.IO) {
                    server.accept().use { client ->
                        client.soTimeout = 5000
                        runCatching { (client as SSLSocket).startHandshake() }
                    }
                }
                failed.await()
                assertTrue(handshake.await().isFailure)
            }
        } finally {
            appender.stopSuspend()
            server.close()
        }
    }

    @Test
    fun trustedCertificateWithWrongHostnameIsRejected() = runBlocking {
        val keyStore = keyStore("wrong.example")
        val server = server(keyStore)
        val trust = trustManager(keyStore)
        val failed = CompletableDeferred<Throwable>()
        val appender = LogstashAppender(config(server).copy(
            tls = { trustManager = trust },
            listeners = listOf(object : LogstashListener {
                override fun connectionFailed(destination: LogstashDestination, cause: Throwable) { failed.complete(cause) }
            })
        ))
        try {
            withTimeout(10.seconds) {
                val handshake = async(Dispatchers.IO) {
                    server.accept().use { client ->
                        client.soTimeout = 5000
                        runCatching { (client as SSLSocket).startHandshake() }
                    }
                }
                failed.await()
                assertTrue(handshake.await().isFailure)
            }
        } finally {
            appender.stopSuspend()
            server.close()
        }
    }

    private fun keyStore(domain: String = "localhost"): KeyStore = buildKeyStore {
        certificate("logstash") {
            password = this@LogstashTlsTest.password
            domains = listOf(domain)
        }
    }

    private fun trustManager(keyStore: KeyStore): X509TrustManager =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore)
        }.trustManagers.filterIsInstance<X509TrustManager>().single()

    private fun server(keyStore: KeyStore): SSLServerSocket {
        val keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore, password.toCharArray())
        }
        val context = SSLContext.getInstance("TLS").apply { init(keys.keyManagers, arrayOf(trustManager(keyStore)), null) }
        return (context.serverSocketFactory.createServerSocket(0, 50, InetAddress.getByName("127.0.0.1")) as SSLServerSocket).apply {
            soTimeout = 5000
            enabledProtocols = arrayOf("TLSv1.2")
        }
    }

    private fun config(server: SSLServerSocket) = LogstashConfig(
        destinations = listOf(LogstashDestination("localhost", server.localPort)),
        reconnectionDelay = 100.milliseconds,
        connectionTimeout = 3.seconds,
        writeTimeout = 3.seconds,
        flushTimeout = 10.seconds,
        shutdownGracePeriod = 200.milliseconds
    )
}