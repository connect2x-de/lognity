package de.connect2x.lognity.test

import de.connect2x.lognity.api.ansi.AnsiScope
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker

internal expect class AsyncTestLogger(config: Config, context: Context) : Logger {
    override val config: Config
    override val context: Context
    override var level: Level
    override var isEnabled: Boolean

    override fun log(level: Level, message: AnsiScope.() -> Any?)
    override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any?)
}