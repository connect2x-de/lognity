package de.connect2x.lognity.config.condition

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Transient

abstract class AbstractSerializableCondition : SerializableCondition {
    @Transient
    protected lateinit var _config: SerializableConfig
    override var config: SerializableConfig
        get() = _config
        set(value) {
            _config = value
        }
}