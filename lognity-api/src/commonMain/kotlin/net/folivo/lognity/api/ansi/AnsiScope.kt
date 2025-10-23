package net.folivo.lognity.api.ansi

object AnsiScope {
    /**
     * Insert the given ANSI sequence before this string and return
     * the newly created [AnsiString].
     *
     * @param sequence The ANSI sequence to insert before this string.
     * @return A new [AnsiString] containing the given sequence followed by the data of this string.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun String.with(sequence: AnsiSequence): AnsiString = AnsiString("$sequence$this")

    /**
     * Insert a reset ANSI escape code after this string and return
     * the newly created string as an [AnsiString].
     *
     * @return A new [AnsiString] containing the data of this string followed by a reset escape code.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun String.reset(): AnsiString = AnsiString("$this${AnsiSequence.reset}")
}