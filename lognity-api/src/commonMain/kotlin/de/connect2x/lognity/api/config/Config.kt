package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.sanitization.SanitizationMode

/**
 * The immutable configuration of a given [Logger] instance.
 * Mainly used for storing references to all appenders used by the logger.
 *
 * @property initialLevel The initial log level.
 * @property initialEnableState The initial enabled state.
 * @property appenders The list of appenders.
 * @property overrides A list of per-instance Logger overrides.
 * @property sanitizationMode How any type of secret in log messages should be treated.
 * @property levelColors A map of ANSI sequences used to color each different log level.
 */
@ConsistentCopyVisibility
data class Config internal constructor( // @formatter:off
    val initialLevel: Level = Level.default,
    val initialEnableState: Boolean = true,
    val appenders: List<Appender> = emptyList(),
    val overrides: List<Override> = emptyList(),
    val sanitizationMode: SanitizationMode = SanitizationMode.OBFUSCATE,
    val levelColors: LevelColors = LevelColors.optimal()
)
