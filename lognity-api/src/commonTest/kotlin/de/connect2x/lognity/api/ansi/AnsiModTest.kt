package de.connect2x.lognity.api.ansi

import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiModTest {
    @Test
    fun `toString returns numeric value as string`() {
        assertEquals("0", AnsiMod.default.toString())
        assertEquals("1", AnsiMod.bold.toString())
        assertEquals("2", AnsiMod.faint.toString())
        assertEquals("3", AnsiMod.italic.toString())
        assertEquals("4", AnsiMod.underline.toString())
        assertEquals("5", AnsiMod.slowBlink.toString())
        assertEquals("6", AnsiMod.rapidBlink.toString())
        assertEquals("7", AnsiMod.invert.toString())
    }

    @Test
    fun `invoke with AnsiFg returns correct AnsiSequence`() {
        val mod = AnsiMod.bold
        val fg = AnsiFg.red
        val sequence = mod(fg)

        // ESC[<mod>;<fg>m
        val expected = "${AnsiSequence.ESC}[1;31m"
        assertEquals(expected, sequence.toString())
    }

    @Test
    fun `invoke with AnsiBg returns correct AnsiSequence`() {
        val mod = AnsiMod.italic
        val bg = AnsiBg.blue
        val sequence = mod(bg)

        // ESC[<mod>;<bg>m
        val expected = "${AnsiSequence.ESC}[3;44m"
        assertEquals(expected, sequence.toString())
    }

    @Test
    fun `predefined constants have expected values`() {
        assertEquals("0", AnsiMod.default.toString())
        assertEquals("1", AnsiMod.bold.toString())
        assertEquals("2", AnsiMod.faint.toString())
        assertEquals("3", AnsiMod.italic.toString())
        assertEquals("4", AnsiMod.underline.toString())
        assertEquals("5", AnsiMod.slowBlink.toString())
        assertEquals("6", AnsiMod.rapidBlink.toString())
        assertEquals("7", AnsiMod.invert.toString())
    }
}
