package de.connect2x.lognity.api.marker

import de.connect2x.lognity.api.backend.Backend

/**
 * Represents a log marker to tag messages with to allow
 * operations like filtering of the messages.
 */
interface Marker {
    /**
     * The internal key the marker is referenced by in cache.
     */
    val key: String

    /**
     * The name of the marker that is printed when {{marker}} is used.
     */
    val name: String

    /**
     * When true means that all messages with this marker will be logged.
     */
    var isEnabled: Boolean
}

/**
 * Creates a default log marker which is enabled by default.
 *
 * @param key The key the marker is identified by.
 * @param name The name of the marker that is actually printed when {{marker}} is used.
 * @param isEnabled When true means that all messages with this marker will be logged.
 * @return A new log marker instance with the given key, name and state.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Marker( // @formatter:off
    key: String,
    name: String = key,
    isEnabled: Boolean = true
): Marker = Backend.createMarker(key, name, isEnabled) // @formatter:on