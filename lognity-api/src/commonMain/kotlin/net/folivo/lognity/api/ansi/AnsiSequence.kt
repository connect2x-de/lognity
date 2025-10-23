package net.folivo.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * Represents one or more ANSI escape codes.
 */
@JvmInline
value class AnsiSequence @PublishedApi internal constructor(@PublishedApi internal val value: String) {
    companion object {
        const val ESC: Char = '\u001b'
        val reset: AnsiSequence = AnsiSequence("$ESC[0m")
    }

    /**
     * Concatenate this ANSI sequence with the given sequence.
     *
     * @param other The sequence to concatenate this sequence with.
     * @return A new ANSI sequence containing all data from this sequence followed
     *  by the data of the other sequence.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: AnsiSequence): AnsiSequence = AnsiSequence("$value$other")

    override fun toString(): String = value
}