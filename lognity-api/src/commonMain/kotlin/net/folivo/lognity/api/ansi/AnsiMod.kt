package net.folivo.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * Text style modifier for ANSI sequences (e.g., bold, italic, underline).
 *
 * Use [invoke] with an [AnsiFg] or [AnsiBg] to build an [AnsiSequence].
 */
@JvmInline
value class AnsiMod @PublishedApi internal constructor(@PublishedApi internal val value: Int) {
    companion object { // @formatter:off
        /** Default style/reset (code 0). */
        val default: AnsiMod    = AnsiMod(0)
        /** Bold text (code 1). */
        val bold: AnsiMod       = AnsiMod(1)
        /** Faint text (code 2). */
        val faint: AnsiMod      = AnsiMod(2)
        /** Italic text (code 3). */
        val italic: AnsiMod     = AnsiMod(3)
        /** Underlined text (code 4). */
        val underline: AnsiMod  = AnsiMod(4)
        /** Slow blink (code 5). */
        val slowBlink: AnsiMod  = AnsiMod(5)
        /** Rapid blink (code 6). */
        val rapidBlink: AnsiMod = AnsiMod(6)
        /** Inverted colors (code 7). */
        val invert: AnsiMod     = AnsiMod(7)
    } // @formatter:on

    /**
     * Build an [AnsiSequence] that applies this text modifier with a foreground color.
     *
     * @param color foreground color to apply alongside this modifier
     * @return an ANSI sequence for modifier + foreground
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun invoke(color: AnsiFg): AnsiSequence = AnsiSequence("${AnsiSequence.ESC}[$value;${color}m")

    /**
     * Build an [AnsiSequence] that applies this text modifier with a background color.
     *
     * @param color background color to apply alongside this modifier
     * @return an ANSI sequence for modifier + background
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun invoke(color: AnsiBg): AnsiSequence = AnsiSequence("${AnsiSequence.ESC}[$value;${color}m")

    /** Returns the raw modifier code as a string. */
    override fun toString(): String = value.toString()
}