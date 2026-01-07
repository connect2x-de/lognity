package de.connect2x.lognity.config

/**
 * DSL marker for the serializable Lognity configuration.
 *
 * This annotation prevents accidental access to members of outer receivers
 * within the configuration DSL.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class SerializableConfigDsl
