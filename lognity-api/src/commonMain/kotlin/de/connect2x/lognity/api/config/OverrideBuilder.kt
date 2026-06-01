package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.logger.Level

/**
 * DSL builder for creating [Override] instances.
 */
@ConfigDsl
class OverrideBuilder @PublishedApi internal constructor() {
    private var condition: OverrideCondition = OverrideCondition.never

    /**
     * The log level to set if the condition is met, or null to keep the original level.
     */
    var level: Level? = null

    /**
     * The enable state to set if the condition is met, or null to keep the original state.
     */
    var enableState: Boolean? = null

    /**
     * Sets the condition that must be met for this override to be applied.
     *
     * @param condition the condition to check.
     */
    fun applyWhen(condition: OverrideCondition) {
        this.condition = condition
    }

    @PublishedApi
    internal fun build(): Override = Override( // @formatter:off
        condition = condition,
        level = level,
        enableState = enableState,
    ) // @formatter:on
}

/**
 * Type alias for a function that configures an [OverrideBuilder].
 */
typealias OverrideSpec = OverrideBuilder.() -> Unit
