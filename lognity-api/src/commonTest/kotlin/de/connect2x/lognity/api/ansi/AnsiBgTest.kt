package de.connect2x.lognity.api.ansi

import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiBgTest {
    @Test
    fun `toString returns numeric value as string`() {
        assertEquals("49", AnsiBg.default.toString())
        assertEquals("40", AnsiBg.black.toString())
        assertEquals("41", AnsiBg.red.toString())
        assertEquals("42", AnsiBg.green.toString())
        assertEquals("43", AnsiBg.yellow.toString())
        assertEquals("44", AnsiBg.blue.toString())
        assertEquals("45", AnsiBg.purple.toString())
        assertEquals("46", AnsiBg.cyan.toString())
        assertEquals("47", AnsiBg.white.toString())

        assertEquals("100", AnsiBg.hiBlack.toString())
        assertEquals("101", AnsiBg.hiRed.toString())
        assertEquals("102", AnsiBg.hiGreen.toString())
        assertEquals("103", AnsiBg.hiYellow.toString())
        assertEquals("104", AnsiBg.hiBlue.toString())
        assertEquals("105", AnsiBg.hiPurple.toString())
        assertEquals("106", AnsiBg.hiCyan.toString())
        assertEquals("107", AnsiBg.hiWhite.toString())
    }
}
