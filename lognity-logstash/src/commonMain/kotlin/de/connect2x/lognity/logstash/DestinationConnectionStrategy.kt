package de.connect2x.lognity.logstash

import de.connect2x.lognity.api.ExperimentalLoggingApi
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Strategies are invoked only by the writer; a null connection duration denotes a failed attempt. */
@ExperimentalLoggingApi
interface DestinationConnectionStrategy {
    fun selectDestination(destinationCount: Int, previousIndex: Int, connectionDuration: Duration?): Int
    fun connectionTTL(destinationIndex: Int): Duration? = null
}

@ExperimentalLoggingApi
class PreferPrimaryStrategy(
    val minConnectionTimeBeforePrimary: Duration = 10.seconds, val secondaryConnectionTTL: Duration? = null
) : DestinationConnectionStrategy {
    init {
        require(minConnectionTimeBeforePrimary.isFinite() && minConnectionTimeBeforePrimary >= Duration.ZERO)
        validateConnectionTTL(secondaryConnectionTTL)
    }

    override fun selectDestination(destinationCount: Int, previousIndex: Int, connectionDuration: Duration?): Int =
        if (connectionDuration != null && connectionDuration >= minConnectionTimeBeforePrimary) 0
        else (previousIndex + 1) % destinationCount

    override fun connectionTTL(destinationIndex: Int): Duration? =
        if (destinationIndex == 0) null else secondaryConnectionTTL
}

@ExperimentalLoggingApi
class RoundRobinStrategy(val connectionTTL: Duration? = null) : DestinationConnectionStrategy {
    init {
        validateConnectionTTL(connectionTTL)
    }

    override fun selectDestination(destinationCount: Int, previousIndex: Int, connectionDuration: Duration?): Int =
        (previousIndex + 1) % destinationCount

    override fun connectionTTL(destinationIndex: Int): Duration? = connectionTTL
}

@ExperimentalLoggingApi
class RandomDestinationStrategy(
    val connectionTTL: Duration? = null, private val random: Random = Random.Default
) : DestinationConnectionStrategy {
    init {
        validateConnectionTTL(connectionTTL)
    }

    override fun selectDestination(destinationCount: Int, previousIndex: Int, connectionDuration: Duration?): Int =
        random.nextInt(destinationCount)

    override fun connectionTTL(destinationIndex: Int): Duration? = connectionTTL
}

@ExperimentalLoggingApi
private fun validateConnectionTTL(duration: Duration?) {
    require(duration == null || duration.isFinite() && duration > Duration.ZERO) { "Connection TTL must be positive and finite" }
}