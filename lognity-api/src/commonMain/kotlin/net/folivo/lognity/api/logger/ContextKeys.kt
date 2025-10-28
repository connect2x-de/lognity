package net.folivo.lognity.api.logger

import net.folivo.lognity.api.marker.Marker

/**
 * Common keys for [Context] used by Lognity internally.
 */
object ContextKeys {
    val name: Context.Key<String> = Context.Key.create("name")
    val defaultMarker: Context.Key<Marker> = Context.Key.create("default_marker")
}