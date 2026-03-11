package de.connect2x.lognity.config.override

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Serializable log configuration override.
 *
 * @property condition the condition that must be met for this override to be applied.
 * @property level the log level to use if the condition is met, or null to keep the original level.
 * @property enableState the enable state to use if the condition is met, or null to keep the original state.
 */
@Serializable
data class SerializableOverride(
    val condition: SerializableOverrideCondition,
    val level: RefOrValue<Level>? = null,
    @SerialName("enabled") val enableState: RefOrValue<Boolean>? = null
) {
    @Transient
    private lateinit var _config: SerializableConfig

    /**
     * The configuration this override belongs to.
     */
    var config: SerializableConfig
        get() = _config
        set(value) {
            condition.config = value
            _config = value
        }
}