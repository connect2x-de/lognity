package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.EmptyContext
import de.connect2x.lognity.api.marker.Marker

/**
 * A no-operation implementation of [Logger].
 *
 * This logger intentionally suppresses all log output. It is useful when you want to
 * disable logging entirely (e.g., in tests, benchmarks, or minimal environments), or as a
 * safe default when no actual backend should be used.
 */
object NoopLogger : Logger {
    override val config: Config = Config()
    override val context: Context = EmptyContext
    override var level: Level = Level.INFO
    override var isEnabled: Boolean = false

    override fun log(level: Level, message: MessageProvider) = Unit
    override fun log(marker: Marker?, level: Level, message: MessageProvider) = Unit
}