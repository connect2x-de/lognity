package net.folivo.lognity.api.config

import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.logger.Level

/**
 * The immutable configuration of a given [net.folivo.lognity.api.logger.Logger] instance.
 * Mainly used for storing references to all appenders used by the logger.
 */
@ConsistentCopyVisibility
data class Config internal constructor( // @formatter:off
    val initialLevel: Level = Level.default,
    val initialEnableState: Boolean = true,
    val appenders: List<Appender> = emptyList()
)