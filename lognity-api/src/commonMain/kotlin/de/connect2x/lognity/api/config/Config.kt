package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger

/**
 * The immutable configuration of a given [Logger] instance.
 * Mainly used for storing references to all appenders used by the logger.
 */
@ConsistentCopyVisibility
data class Config internal constructor( // @formatter:off
    val initialLevel: Level = Level.default,
    val initialEnableState: Boolean = true,
    val appenders: List<Appender> = emptyList()
)