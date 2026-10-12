package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.EmptyContext
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue

internal class ConditionTestLogger : Logger {
    override val config: Config = Config {}
    override val context: Context = EmptyContext
    override var level: Level = Level.INFO
    override var isEnabled: Boolean = true

    override fun log(level: Level, message: MessageProvider) = error("Unexpected logging")
    override fun log(marker: Marker?, level: Level, message: MessageProvider) = error("Unexpected logging")
}

internal class RecordingCondition(
    private val result: Boolean,
    override val name: RefOrValue<String?> = RefOrValue.Value(null)
) : AbstractSerializableCondition() {
    data class Invocation(val logger: Logger, val level: Level, val message: String, val marker: Marker?)

    val invocations: MutableList<Invocation> = mutableListOf()

    override fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean {
        invocations += Invocation(logger, level, message, marker)
        return result
    }
}