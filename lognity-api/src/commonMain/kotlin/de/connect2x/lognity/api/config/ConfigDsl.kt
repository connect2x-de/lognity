package de.connect2x.lognity.api.config

/**
 * DSL marker for the Lognity configuration DSL.
 *
 * This annotation prevents accidental access to members of outer receivers
 * within the configuration DSL.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class ConfigDsl
