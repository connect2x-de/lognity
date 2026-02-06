package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.SerializableFilter
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Transient

/**
 * Serializable description of an appender used by Lognity.
 *
 * This is the polymorphic base type for all appender definitions that can
 * appear in the JSON configuration.
 */
@Polymorphic
interface SerializableAppender {
    @Transient
    var config: SerializableConfig

    /**
     * The log pattern to use for this appender.
     */
    val pattern: RefOrValue<String>

    /**
     * The name of the formatter to use for this appender.
     */
    val formatter: RefOrValue<String>

    /**
     * The filter to apply to this appender.
     */
    val filter: RefOrValue<SerializableFilter>

    /**
     * Optional name for this appender.
     */
    val name: RefOrValue<String?>
}