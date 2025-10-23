package net.folivo.lognity.slf4j

import net.folivo.lognity.api.marker.Marker
import org.slf4j.IMarkerFactory
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.Marker as Slf4jMarker

/**
 * SLF4J [IMarkerFactory] implementation backed by Lognity markers.
 *
 * It provides and caches SLF4J [org.slf4j.Marker] instances and bridges them to
 * Lognity's [net.folivo.lognity.api.marker.Marker].
 */
object LognitySlf4jMarkerFactory : IMarkerFactory {
    private val markers: ConcurrentHashMap<String, LognitySlf4jMarker> = ConcurrentHashMap()

    /** Returns a bridged SLF4J marker for the given [name]. */
    override fun getMarker(name: String): Slf4jMarker {
        return markers.getOrPut(name) {
            LognitySlf4jMarker(Marker(name))
        }
    }

    /** Whether a marker with [name] exists in the local cache. */
    override fun exists(name: String): Boolean = markers.containsKey(name)
    /** No-op detach, kept for SLF4J API compatibility. Always returns true. */
    override fun detachMarker(name: String): Boolean = true
    /** Returns a non-cached marker instance for [name]. */
    override fun getDetachedMarker(name: String): Slf4jMarker = getMarker(name)
}