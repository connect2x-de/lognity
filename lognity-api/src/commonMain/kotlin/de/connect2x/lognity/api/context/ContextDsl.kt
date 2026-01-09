package de.connect2x.lognity.api.context

/**
 * DSL marker for the Lognity context DSL.
 *
 * This annotation prevents accidental access to members of outer receivers
 * within the configuration DSL.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class ContextDsl
