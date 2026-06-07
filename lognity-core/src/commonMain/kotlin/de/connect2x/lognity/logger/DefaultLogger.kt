package de.connect2x.lognity.logger

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.logger.invoke
import de.connect2x.lognity.api.marker.Marker
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.time.Clock

/**
 * Default implementation of the [Logger] interface.
 *
 * This implementation uses atomic variables to manage the log level and enabled state,
 * ensuring thread-safety for these properties.
 */
open class DefaultLogger( // @formatter:off
    override val config: Config,
    override val context: Context
) : Logger { // @formatter:on
    private val _level: AtomicReference<Level> = AtomicReference(config.initialLevel)

    override var level: Level
        get() = _level.load()
        set(value) {
            _level.store(value)
        }

    private val _isEnabled: AtomicBoolean = AtomicBoolean(config.initialEnableState)

    override var isEnabled: Boolean
        get() = _isEnabled.load()
        set(value) {
            _isEnabled.store(value)
        }

    override fun log(level: Level, message: MessageProvider) {
        val marker = context[Logger.DefaultMarker]?.marker

        // Evaluate level
        var targetLevel = Backend.overrideLevel ?: this.level
        var isEnabled = isEnabled
        for (override in config.overrides) {
            if (!override.condition(this, level, marker)) continue
            override.level?.let { targetLevel = it }
            override.enableState?.let { isEnabled = it }
            break // The first override that matches wins
        }
        if (!isEnabled || level < targetLevel) return

        // Evaluate marker
        if (marker?.isEnabled == false) return

        val messageContent = message(this) ?: "null"
        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        for (appender in config.appenders) {
            appender.append(
                this,
                level,
                appender.formatter(this, appender, level, messageContent, marker, timestamp),
                null,
            )
        }
    }

    override fun log(marker: Marker?, level: Level, message: MessageProvider) {
        val actualMarker = marker ?: context[Logger.DefaultMarker]?.marker

        // Evaluate level
        var targetLevel = Backend.overrideLevel ?: this.level
        var isEnabled = isEnabled
        for (override in config.overrides) {
            if (!override.condition(this, level, marker)) continue
            override.level?.let { targetLevel = it }
            override.enableState?.let { isEnabled = it }
            break // The first override that matches wins
        }
        if (!isEnabled || level < targetLevel) return

        // Evaluate marker
        if (actualMarker?.isEnabled == false) return

        val messageContent = message(this) ?: "null"

        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        for (appender in config.appenders) {
            appender.append(
                this,
                level,
                appender.formatter(this, appender, level, messageContent, marker, timestamp),
                marker,
            )
        }
    }
}
