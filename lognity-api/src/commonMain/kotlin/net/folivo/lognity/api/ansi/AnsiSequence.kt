package net.folivo.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * Represents one or more ANSI escape codes.
 *
 * You can concatenate sequences using the [plus] operator or by string
 * interpolation when building higher level helpers.
 */
@JvmInline
value class AnsiSequence @PublishedApi internal constructor(@PublishedApi internal val value: String) {
    companion object {
        /** Escape character used to start ANSI sequences. */
        const val ESC: Char = '\u001b'

        /** Sequence that resets all styles and colors. */
        val reset: AnsiSequence = AnsiSequence("$ESC[0m")
    }

    /** Concatenate two ANSI sequences. */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: AnsiSequence): AnsiSequence = AnsiSequence("$value$other")

    /** Return the raw escape sequence string. */
    override fun toString(): String = value
}