package de.connect2x.lognity.config.override

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Transient

/**
 * Abstract base class for [SerializableOverrideCondition] implementations.
 */
abstract class AbstractSerializableOverrideCondition : SerializableOverrideCondition {
    @Transient
    private lateinit var _config: SerializableConfig

    /**
     * The configuration this condition belongs to.
     */
    override var config: SerializableConfig
        get() = _config
        set(value) {
            _config = value
        }
}