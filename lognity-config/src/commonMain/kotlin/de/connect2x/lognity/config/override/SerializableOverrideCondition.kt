package de.connect2x.lognity.config.override

import de.connect2x.lognity.api.config.OverrideCondition
import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Transient

/**
 * Interface for serializable log configuration override conditions.
 */
interface SerializableOverrideCondition : OverrideCondition {
    /**
     * The configuration this condition belongs to.
     */
    @Transient
    var config: SerializableConfig
}