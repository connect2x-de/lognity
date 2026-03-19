package de.connect2x.lognity.api.ansi

import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiStringTest {
    @Test
    fun `cleanString removes ANSI sequences`() {
        val boldRed = "${AnsiSequence.ESC}[1;31m"
        val reset = "${AnsiSequence.ESC}[0m"
        val s = AnsiString("${boldRed}Hello${reset} World")
        assertEquals("Hello World", s.cleanString())
    }

    @Test
    fun `plus operator appends string`() {
        val s = "Hello".toAnsi() + " World"
        assertEquals("Hello World", s.toString())
        assertEquals(AnsiString("Hello World"), s)
    }

    @Test
    fun `plus operator appends AnsiString`() {
        val s1 = "Hello".toAnsi()
        val s2 = " World".toAnsi()
        val result = s1 + s2
        assertEquals("Hello World", result.toString())
    }

    @Test
    fun `with operator prefixes with AnsiSequence`() {
        val seq = AnsiFg.red on AnsiBg.black
        val s = "Hello".toAnsi() with seq
        assertEquals("${seq}Hello", s.toString())
    }

    @Test
    fun `reset appends reset sequence`() {
        val s = "Hello".toAnsi().reset()
        assertEquals("Hello${AnsiSequence.reset}", s.toString())
    }

    @Test
    fun `toAnsi converts String to AnsiString`() {
        val s = "test".toAnsi()
        assertEquals("test", s.toString())
    }

    @Test
    fun `CharSequence implementation works`() {
        val s = "Hello".toAnsi()
        assertEquals(5, s.length)
        assertEquals('H', s[0])
        assertEquals("ell", s.subSequence(1, 4).toString())
    }

    @Test
    fun `cleanString handles complex sequences`() {
        // Test with different endings: [ABCDEFGHIJKLm]
        val sequences = listOf(
            "${AnsiSequence.ESC}[1A",
            "${AnsiSequence.ESC}[2B",
            "${AnsiSequence.ESC}[3C",
            "${AnsiSequence.ESC}[4D",
            "${AnsiSequence.ESC}[5E",
            "${AnsiSequence.ESC}[6F",
            "${AnsiSequence.ESC}[7G",
            "${AnsiSequence.ESC}[8H",
            "${AnsiSequence.ESC}[9I",
            "${AnsiSequence.ESC}[10J",
            "${AnsiSequence.ESC}[11K",
            "${AnsiSequence.ESC}[12L",
            "${AnsiSequence.ESC}[31;1;4m"
        )
        for (seq in sequences) {
            val s = AnsiString("${seq}text")
            assertEquals("text", s.cleanString(), "Failed for sequence: $seq")
        }
    }
}
