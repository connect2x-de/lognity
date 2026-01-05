package de.connect2x.lognity.api.ansi

import kotlin.jvm.JvmInline

/**
 * Background color for ANSI escape sequences.
 *
 * Use the predefined constants from the [companion object] to select a color
 * and combine it with a foreground using [AnsiFg.on] or [AnsiFg.boldOn].
 */
@JvmInline
value class AnsiBg @PublishedApi internal constructor(@PublishedApi internal val value: Int) {
    /**
     * Predefined background colors matching standard ANSI color codes.
     *
     * - default: 49
     * - black..white: 40..47
     * - hiBlack..hiWhite (bright variants): 100..107
     */
    companion object {
        // @formatter:off
        /** Reset to the terminal's default background (code 49). */
        val default: AnsiBg  = AnsiBg(49)

        /** Standard black (code 40). */
        val black: AnsiBg    = AnsiBg(40)
        /** Standard red (code 41). */
        val red: AnsiBg      = AnsiBg(41)
        /** Standard green (code 42). */
        val green: AnsiBg    = AnsiBg(42)
        /** Standard yellow (code 43). */
        val yellow: AnsiBg   = AnsiBg(43)
        /** Standard blue (code 44). */
        val blue: AnsiBg     = AnsiBg(44)
        /** Standard magenta/purple (code 45). */
        val purple: AnsiBg   = AnsiBg(45)
        /** Standard cyan (code 46). */
        val cyan: AnsiBg     = AnsiBg(46)
        /** Standard white (code 47). */
        val white: AnsiBg    = AnsiBg(47)

        /** Bright black/gray (code 100). */
        val hiBlack: AnsiBg  = AnsiBg(100)
        /** Bright red (code 101). */
        val hiRed: AnsiBg    = AnsiBg(101)
        /** Bright green (code 102). */
        val hiGreen: AnsiBg  = AnsiBg(102)
        /** Bright yellow (code 103). */
        val hiYellow: AnsiBg = AnsiBg(103)
        /** Bright blue (code 104). */
        val hiBlue: AnsiBg   = AnsiBg(104)
        /** Bright magenta/purple (code 105). */
        val hiPurple: AnsiBg = AnsiBg(105)
        /** Bright cyan (code 106). */
        val hiCyan: AnsiBg   = AnsiBg(106)
        /** Bright white (code 107). */
        val hiWhite: AnsiBg  = AnsiBg(107)
        // @formatter:on
    }

    /** Returns the raw ANSI numeric code as a string. */
    override fun toString(): String = value.toString()
}