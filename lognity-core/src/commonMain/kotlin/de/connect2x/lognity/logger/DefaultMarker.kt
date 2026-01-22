package de.connect2x.lognity.logger

import de.connect2x.lognity.api.marker.Marker
import kotlin.concurrent.atomics.AtomicBoolean

class DefaultMarker( // @formatter:off
    override val key: String,
    override val name: String,
    isEnabled: Boolean
) : Marker { // @formatter:on
    private val _isEnabled: AtomicBoolean = AtomicBoolean(isEnabled)
    override var isEnabled: Boolean
        get() = _isEnabled.load()
        set(value) {
            _isEnabled.store(value)
        }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Marker -> other.key == key && other.name == name && other.isEnabled == isEnabled
            else -> false
        }
    }

    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "LogMarker($key/$name)"
}