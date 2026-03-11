package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.logger.Level

/**
 * Represents a log configuration override.
 *
 * @property condition the condition that must be met for this override to be applied.
 * @property level the log level to use if the condition is met, or null to keep the original level.
 * @property enableState the enable state to use if the condition is met, or null to keep the original state.
 */
data class Override( // @formatter:off
    val condition: OverrideCondition,
    val level: Level?,
    val enableState: Boolean?
) // @formatter:on