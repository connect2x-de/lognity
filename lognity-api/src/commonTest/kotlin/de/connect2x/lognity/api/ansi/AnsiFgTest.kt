package de.connect2x.lognity.api.ansi

import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiFgTest {
    @Test
    fun `toString returns numeric value as string`() {
        assertEquals("39", AnsiFg.default.toString())
        assertEquals("30", AnsiFg.black.toString())
        assertEquals("31", AnsiFg.red.toString())
        assertEquals("32", AnsiFg.green.toString())
        assertEquals("33", AnsiFg.yellow.toString())
        assertEquals("34", AnsiFg.blue.toString())
        assertEquals("35", AnsiFg.purple.toString())
        assertEquals("36", AnsiFg.cyan.toString())
        assertEquals("37", AnsiFg.white.toString())

        assertEquals("90", AnsiFg.hiBlack.toString())
        assertEquals("91", AnsiFg.hiRed.toString())
        assertEquals("92", AnsiFg.hiGreen.toString())
        assertEquals("93", AnsiFg.hiYellow.toString())
        assertEquals("94", AnsiFg.hiBlue.toString())
        assertEquals("95", AnsiFg.hiPurple.toString())
        assertEquals("96", AnsiFg.hiCyan.toString())
        assertEquals("97", AnsiFg.hiWhite.toString())
    }

    @Test
    fun `on returns correct AnsiSequence`() {
        val fg = AnsiFg.red
        val bg = AnsiBg.black
        val result = fg on bg

        // ESC[0;<bg>m + ESC[0;<fg>m
        val expected = "${AnsiSequence.ESC}[0;40m${AnsiSequence.ESC}[0;31m"
        assertEquals(expected, result.toString())
    }

    @Test
    fun `boldOn returns correct AnsiSequence`() {
        val fg = AnsiFg.red
        val bg = AnsiBg.black
        val result = fg boldOn bg

        // ESC[0;<bg>m + ESC[1;<fg>m
        val expected = "${AnsiSequence.ESC}[0;40m${AnsiSequence.ESC}[1;31m"
        assertEquals(expected, result.toString())
    }
}
