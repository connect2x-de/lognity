package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.Polymorphic

/**
 * Base type for all filter conditions used by [de.connect2x.lognity.config.SerializableFilter].
 *
 * Implementations decide based on the log [Level], message text, and optional [Marker].
 */
@Polymorphic
interface SerializableCondition {
    val name: RefOrValue<String?>

    operator fun invoke(level: Level, message: String, marker: Marker?): Boolean
}