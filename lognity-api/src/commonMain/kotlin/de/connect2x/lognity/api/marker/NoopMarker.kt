package de.connect2x.lognity.api.marker

import de.connect2x.lognity.api.marker.NoopMarker.isEnabled
import de.connect2x.lognity.api.marker.NoopMarker.key
import de.connect2x.lognity.api.marker.NoopMarker.name


/**
 * A no-op implementation of [Marker].
 *
 * This marker is used as a safe default when no concrete marker is provided.
 * It has an empty [key] and [name] and is always disabled ([isEnabled] is
 * always `false`). Appenders/formatters can treat it as “no marker”.
 */
object NoopMarker : Marker {
    override val key: String = ""
    override val name: String = ""

    override var isEnabled: Boolean
        get() = false
        set(_) {}
}