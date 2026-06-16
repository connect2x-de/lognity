package de.connect2x.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * A string that may contain ANSI escape sequences.
 *
 * Provides helpers to manipulate and compose ANSI-decorated strings safely.
 */
@JvmInline
value class AnsiString @PublishedApi internal constructor(@PublishedApi internal val value: String) : CharSequence by value {
    companion object {
        /** Pattern for matching ANSI escape sequences in a string. */
        private val pattern: Regex = Regex("""${AnsiSequence.ESC}\[[\w;]+?[ABCDEFGHIJKLm]""")
    }

    /**
     * Remove all ANSI escape sequences and return a plain string.
     *
     * @return A plain string without ANSI sequences.
     */
    fun cleanString(): String = value.replace(pattern, "")

    /**
     * Append a plain [String] to this [AnsiString].
     *
     * @param s The string to append.
     * @return A new [AnsiString] with the appended string.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(s: String): AnsiString = AnsiString("$value$s")

    /**
     * Append another [AnsiString] to this [AnsiString].
     *
     * @param s The ANSI string to append.
     * @return A new [AnsiString] with the appended ANSI string.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(s: AnsiString): AnsiString = AnsiString("$value$s")

    /**
     * Prefix this [AnsiString] with an [AnsiSequence].
     *
     * @param sequence The ANSI sequence to prefix.
     * @return A new [AnsiString] with the prefix.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun with(sequence: AnsiSequence): AnsiString = AnsiString("$sequence$value")

    /**
     * Append [AnsiSequence.reset] to this [AnsiString].
     *
     * @return A new [AnsiString] with the reset sequence appended.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun reset(): AnsiString = AnsiString("$value${AnsiSequence.reset}")

    /** Return the underlying string, including ANSI sequences. */
    override fun toString(): String = value
}

/**
 * Convert this string into an ANSI string.
 *
 * @return A new [AnsiString] instance containing the data of this string.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toAnsi(): AnsiString = AnsiString(this)
