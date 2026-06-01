package de.connect2x.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * Foreground (text) color for ANSI escape sequences.
 *
 * Use the predefined constants from the [companion object] to select a color and
 * combine it with a background via [on] or with bold text via [boldOn].
 *
 * Example:
 * ```kotlin
 * with(AnsiScope) {
 *   val colored = "Error" with (AnsiFg.red boldOn AnsiBg.black)
 * }
 * ```
 */
@JvmInline
value class AnsiFg @PublishedApi internal constructor(@PublishedApi internal val value: Int) {
    /**
     * Predefined foreground colors matching standard ANSI color codes.
     *
     * - default: 39
     * - black..white: 30..37
     * - hiBlack..hiWhite (bright variants): 90..97
     */
    companion object {
        // @formatter:off
        /** Reset to the terminal's default foreground (code 39). */
        val default: AnsiFg  = AnsiFg(39)

        /** Standard black (code 30). */
        val black: AnsiFg    = AnsiFg(30)
        /** Standard red (code 31). */
        val red: AnsiFg      = AnsiFg(31)
        /** Standard green (code 32). */
        val green: AnsiFg    = AnsiFg(32)
        /** Standard yellow (code 33). */
        val yellow: AnsiFg   = AnsiFg(33)
        /** Standard blue (code 34). */
        val blue: AnsiFg     = AnsiFg(34)
        /** Standard magenta/purple (code 35). */
        val purple: AnsiFg   = AnsiFg(35)
        /** Standard cyan (code 36). */
        val cyan: AnsiFg     = AnsiFg(36)
        /** Standard white (code 37). */
        val white: AnsiFg    = AnsiFg(37)

        /** Bright black/gray (code 90). */
        val hiBlack: AnsiFg  = AnsiFg(90)
        /** Bright red (code 91). */
        val hiRed: AnsiFg    = AnsiFg(91)
        /** Bright green (code 92). */
        val hiGreen: AnsiFg  = AnsiFg(92)
        /** Bright yellow (code 93). */
        val hiYellow: AnsiFg = AnsiFg(93)
        /** Bright blue (code 94). */
        val hiBlue: AnsiFg   = AnsiFg(94)
        /** Bright magenta/purple (code 95). */
        val hiPurple: AnsiFg = AnsiFg(95)
        /** Bright cyan (code 96). */
        val hiCyan: AnsiFg   = AnsiFg(96)
        /** Bright white (code 97). */
        val hiWhite: AnsiFg  = AnsiFg(97)
        // @formatter:on
    }

    /**
     * Returns the raw ANSI numeric code as a string.
     */
    override fun toString(): String = value.toString()

    /**
     * Create an [AnsiSequence] that applies this foreground color on the given background using default text style.
     *
     * @param bg the background color to combine with
     * @return an ANSI sequence that sets both background and this foreground
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun on(bg: AnsiBg): AnsiSequence = AnsiSequence(
        "${
            AnsiMod.default(bg)
        }${AnsiMod.default(this)}",
    )

    /**
     * Create an [AnsiSequence] that applies this foreground color in bold on the given background.
     *
     * @param bg the background color to combine with
     * @return an ANSI sequence that sets background and bold foreground
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun boldOn(bg: AnsiBg): AnsiSequence = AnsiSequence(
        "${
            AnsiMod.default(bg)
        }${AnsiMod.bold(this)}",
    )
}
