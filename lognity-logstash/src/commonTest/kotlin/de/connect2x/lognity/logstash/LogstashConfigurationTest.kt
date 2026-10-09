package de.connect2x.lognity.logstash

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class LogstashConfigurationTest {
    @Test
    fun destinationsAcceptHostnamesIpv4AndBracketedIpv6() {
        assertEquals(LogstashDestination("localhost", 4560), LogstashDestination.parse("localhost"))
        assertEquals(LogstashDestination("127.0.0.1", 5044), LogstashDestination.parse(" 127.0.0.1:5044 "))
        assertEquals(LogstashDestination("::1", 4560), LogstashDestination.parse("[::1]"))
        assertEquals(LogstashDestination("::1", 5044), LogstashDestination.parse("[::1]:5044"))
    }

    @Test
    fun invalidDestinationsAreRejected() {
        listOf("", ":5044", "host:", "host:-1", "host:65536", "host:abc", "::1", "[::1", "[::1]oops", "a b").forEach {
            assertFailsWith<IllegalArgumentException>(it) { LogstashDestination.parse(it) }
        }
    }

    @Test
    fun optionsValidateBeforeStartingIo() {
        assertFailsWith<IllegalArgumentException> { LogstashConfig(emptyList()) }
        assertFailsWith<IllegalArgumentException> { config(ringBufferSize = 0) }
        assertFailsWith<IllegalArgumentException> { config(ringBufferSize = 3) }
        assertFailsWith<IllegalArgumentException> { LogstashConfig(destinations, writeBufferSize = -1) }
        assertFailsWith<IllegalArgumentException> { LogstashConfig(destinations, reconnectionDelay = 0.seconds) }
        assertFailsWith<IllegalArgumentException> { LogstashConfig(destinations, connectionTimeout = (-1).seconds) }
        assertFailsWith<IllegalArgumentException> { LogstashConfig(destinations, keepAliveDuration = 0.seconds) }
    }

    @Test
    fun defaultsMatchTcpAppender() {
        val config = config()
        assertEquals(8192, config.ringBufferSize)
        assertEquals(8192, config.writeBufferSize)
        assertEquals(30.seconds, config.reconnectionDelay)
        assertEquals(5.seconds, config.connectionTimeout)
        assertEquals(0.seconds, config.appendTimeout)
        assertEquals(60.seconds, config.shutdownGracePeriod)
        assertEquals("\n", config.keepAliveMessage)
    }

    @Test
    fun strategiesSelectDestinationsAndExpiration() {
        val primary = PreferPrimaryStrategy(secondaryConnectionTTL = 20.seconds)
        assertEquals(0, primary.selectDestination(3, -1, null))
        assertEquals(1, primary.selectDestination(3, 0, null))
        assertEquals(2, primary.selectDestination(3, 1, 9.seconds))
        assertEquals(0, primary.selectDestination(3, 1, 10.seconds))
        assertEquals(null, primary.connectionTTL(0))
        assertEquals(20.seconds, primary.connectionTTL(1))
        val roundRobin = RoundRobinStrategy(50.milliseconds)
        assertEquals(0, roundRobin.selectDestination(3, 2, 100.seconds))
        assertEquals(50.milliseconds, roundRobin.connectionTTL(0))
    }

    @Test
    fun randomStrategyIsReproducibleWithAnInjectedSeed() {
        val first = RandomDestinationStrategy(random = Random(42))
        val second = RandomDestinationStrategy(random = Random(42))
        val expected = List(30) { first.selectDestination(3, -1, null) }
        assertEquals(expected, List(30) { second.selectDestination(3, -1, null) })
        assertEquals(setOf(0, 1, 2), expected.toSet())
    }

    private val destinations = listOf(LogstashDestination("localhost"))

    private fun config(ringBufferSize: Int = 8192) = LogstashConfig(destinations, ringBufferSize = ringBufferSize)
}