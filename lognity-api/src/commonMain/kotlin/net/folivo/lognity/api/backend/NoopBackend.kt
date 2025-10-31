package net.folivo.lognity.api.backend

import net.folivo.lognity.api.config.ConfigSpec
import net.folivo.lognity.api.context.ContextSpec
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.logger.NoopLogger
import net.folivo.lognity.api.marker.Marker
import net.folivo.lognity.api.marker.NoopMarker

/**
 * A no-operation implementation of the [Backend] interface.
 * This implementation doesn't perform any actual logging operations and is useful
 * for situations where logging should be completely disabled or as a default
 * implementation when no other backend is configured.
 */
object NoopBackend : Backend {
    override val name: String = "NOOP"
    override val defaultLevel: Level = Level.WARN
    override val defaultFormatter: Formatter = Formatter.identity

    override var configSpec: ConfigSpec
        get() = {}
        set(value) {}

    override var contextSpec: ContextSpec
        get() = {}
        set(value) {}

    override fun addShutdownHook(hook: () -> Unit) = Unit

    override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker = NoopMarker

    override fun createLogger(name: String?, contextSpec: ContextSpec): Logger = NoopLogger
}
