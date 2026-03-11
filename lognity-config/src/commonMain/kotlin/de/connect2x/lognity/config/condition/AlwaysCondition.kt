package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Condition that always evaluates to true.
 */
@SerialName("always")
@Serializable
data object AlwaysCondition : AbstractSerializableCondition() {
    @Transient // We don't need to save this for our singleton
    override val name: RefOrValue<String?> = RefOrValue.Value("always")

    override operator fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean = true
}