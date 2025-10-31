package net.folivo.lognity.logger

import net.folivo.lognity.api.ansi.AnsiScope
import net.folivo.lognity.api.config.Config
import net.folivo.lognity.api.context.Context
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.api.marker.Marker
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@PublishedApi
internal class DefaultLogger( // @formatter:off
    override val config: Config,
    override val context: Context
) : Logger { // @formatter:on
    private val _level: AtomicInt = AtomicInt(config.initialLevel.ordinal)
    override var level: Level
        get() = Level.entries[_level.load()]
        set(value) {
            _level.store(value.ordinal)
        }

    private val _isEnabled: AtomicBoolean = AtomicBoolean(config.initialEnableState)
    override var isEnabled: Boolean
        get() = _isEnabled.load()
        set(value) {
            _isEnabled.store(value)
        }

    override fun log(level: Level, message: AnsiScope.() -> Any) {
        if (level < this.level) return
        val marker = context[Logger.DefaultMarker]?.marker
        if (marker?.isEnabled == false) return
        val messageContent = message(AnsiScope)
        for (appender in config.appenders) {
            appender.append(
                this, level, appender.formatter(this, level, messageContent, marker, appender.pattern), null
            )
        }
    }

    override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any) {
        if (level < this.level) return
        val actualMarker = marker ?: context[Logger.DefaultMarker]?.marker
        if (actualMarker?.isEnabled == false) return
        val messageContent = message(AnsiScope)
        for (appender in config.appenders) {
            appender.append(
                this, level, appender.formatter(this, level, messageContent, marker, appender.pattern), marker
            )
        }
    }
}