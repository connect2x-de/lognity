package net.folivo.lognity.api.logger

import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.marker.Marker

/**
 * A no-operation implementation of [Logger].
 *
 * This logger intentionally suppresses all log output. It is useful when you want to
 * disable logging entirely (e.g., in tests, benchmarks, or minimal environments), or as a
 * safe default when no actual backend should be used.
 */
object NoopLogger : Logger {
    override val config: Config = Config()
    override val context: Context = context { }
    override var level: Level = Level.INFO
    override var isEnabled: Boolean = false

    override fun log(level: Level, message: AnsiScope.() -> Any) = Unit

    override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any) = Unit
}