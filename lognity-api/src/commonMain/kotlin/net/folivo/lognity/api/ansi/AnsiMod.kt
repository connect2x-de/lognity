package net.folivo.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * A list of all commonly available ANSI modifiers.
 */
@JvmInline
value class AnsiMod @PublishedApi internal constructor(@PublishedApi internal val value: Int) {
    companion object { // @formatter:off
        val default: AnsiMod    = AnsiMod(0)
        val bold: AnsiMod       = AnsiMod(1)
        val faint: AnsiMod      = AnsiMod(2)
        val italic: AnsiMod     = AnsiMod(3)
        val underline: AnsiMod  = AnsiMod(4)
        val slowBlink: AnsiMod  = AnsiMod(5)
        val rapidBlink: AnsiMod = AnsiMod(6)
        val invert: AnsiMod     = AnsiMod(7)
    } // @formatter:on

    /**
     * Turn the given foreground colors into a new [AnsiSequence] using this modifier.
     *
     * @param color The foreground color to join with this modifier.
     * @return A new ANSI sequence containing a new escape code for this modifier and the given color.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun invoke(color: AnsiFg): AnsiSequence = AnsiSequence("${AnsiSequence.ESC}[$value;${color}m")

    /**
     * Turn the given background colors into a new [AnsiSequence] using this modifier.
     *
     * @param color The background color to join with this modifier.
     * @return A new ANSI sequence containing a new escape code for this modifier and the given color.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun invoke(color: AnsiBg): AnsiSequence = AnsiSequence("${AnsiSequence.ESC}[$value;${color}m")

    override fun toString(): String = value.toString()
}