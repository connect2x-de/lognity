package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import kotlinx.serialization.Polymorphic

/**
 * Serializable description of an appender used by Lognity.
 *
 * This is the polymorphic base type for all appender definitions that can
 * appear in the JSON configuration.
 */
@Polymorphic
interface SerializableAppender {
    val pattern: String
    val formatter: String
    val filter: SerializableFilter
    val name: String?
}