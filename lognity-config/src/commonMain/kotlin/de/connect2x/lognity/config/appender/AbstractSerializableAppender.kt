package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableConfig
import kotlinx.serialization.Transient

abstract class AbstractSerializableAppender : SerializableAppender {
    @Transient
    private lateinit var _config: SerializableConfig
    override var config: SerializableConfig
        get() = _config
        set(value) {
            filter.resolveTemplate(value).config = value // Propagate to filter instances
            _config = value
        }
}