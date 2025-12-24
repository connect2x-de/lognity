package de.connect2x.lognity.slf4j

import de.connect2x.lognity.api.marker.Marker
import java.util.concurrent.ConcurrentLinkedDeque
import org.slf4j.Marker as Slf4jMarker

internal class LognitySlf4jMarker(
    val delegate: Marker
) : Slf4jMarker {
    private val children: ConcurrentLinkedDeque<Slf4jMarker> = ConcurrentLinkedDeque()

    override fun getName(): String = delegate.name

    override fun add(reference: Slf4jMarker) {
        if (reference in children) return
        children += reference
    }

    override fun remove(reference: Slf4jMarker): Boolean {
        if (reference !in children) return false
        children -= reference
        return true
    }

    @Deprecated("Deprecated in favour of hasReferences()", replaceWith = ReplaceWith("hasReferences()"))
    override fun hasChildren(): Boolean = children.isNotEmpty()

    override fun hasReferences(): Boolean = children.isNotEmpty()
    override fun iterator(): Iterator<Slf4jMarker> = children.iterator()
    override fun contains(other: Slf4jMarker): Boolean = other in children
    override fun contains(name: String): Boolean = children.any { it.name == name }
}

/**
 * Converts a Lognity [Marker] to an SLF4J [org.slf4j.Marker].
 *
 * Use this when interacting with libraries that expect SLF4J markers.
 */
fun Marker.asSlf4jMarker(): Slf4jMarker = LognitySlf4jMarker(this)

/**
 * Converts an SLF4J [org.slf4j.Marker] to a Lognity [Marker].
 *
 * If the given marker is already a bridged instance, its original Lognity marker is returned.
 */
fun Slf4jMarker.asLognityMarker(): Marker = when (this) {
    is LognitySlf4jMarker -> delegate
    else -> Marker(name)
}