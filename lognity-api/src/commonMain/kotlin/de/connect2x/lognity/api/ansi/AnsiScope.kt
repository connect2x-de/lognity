package de.connect2x.lognity.api.ansi

/**
 * Scope helper to build ANSI strings with a concise DSL.
 *
 * Example:
 * ```kotlin
 * with(AnsiScope) {
 *   val msg = "Hello" with (AnsiFg.green on AnsiBg.black)
 *   val reset = msg.reset()
 * }
 * ```
 */
object AnsiScope {
    /** Prefix this string with the given [AnsiSequence]. */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun String.with(sequence: AnsiSequence): AnsiString = AnsiString("$sequence$this")

    /** Append [AnsiSequence.reset] after this string. */
    @Suppress("NOTHING_TO_INLINE")
    inline fun String.reset(): AnsiString = AnsiString("$this${AnsiSequence.reset}")
}