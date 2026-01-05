package de.connect2x.lognity.api.backend

import de.connect2x.lognity.api.config.ConfigSpec
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.NoopLogger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.api.marker.NoopMarker

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
    override val platform: Platform = Platform.UNKNOWN

    override var configSpec: ConfigSpec
        get() = {}
        set(_) {}

    override var contextSpec: ContextSpec
        get() = {}
        set(_) {}

    override fun addShutdownHook(hook: () -> Unit) = Unit

    override fun createMarker(key: String, name: String, isEnabled: Boolean): Marker = NoopMarker

    override fun createLogger(name: String?, contextSpec: ContextSpec): Logger = NoopLogger
}
