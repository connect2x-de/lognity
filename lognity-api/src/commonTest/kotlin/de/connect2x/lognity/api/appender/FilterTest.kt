package de.connect2x.lognity.api.appender

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.NoopLogger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.api.marker.NoopMarker
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterTest {
    private val logger: Logger = NoopLogger
    private val message: String = "test message"
    private val marker: Marker = NoopMarker

    @Test
    fun `always filter should always return true`() {
        val filter = Filter.always
        assertTrue(filter(logger, Level.TRACE, message, marker))
        assertTrue(filter(logger, Level.DEBUG, message, null))
        assertTrue(filter(logger, Level.INFO, message, marker))
        assertTrue(filter(logger, Level.WARN, message, null))
        assertTrue(filter(logger, Level.ERROR, message, marker))
        assertTrue(filter(logger, Level.FATAL, message, null))
    }

    @Test
    fun `levels filter should only allow specified levels`() {
        val filter = Filter.levels(Level.INFO, Level.ERROR)
        assertFalse(filter(logger, Level.TRACE, message, null))
        assertFalse(filter(logger, Level.DEBUG, message, null))
        assertTrue(filter(logger, Level.INFO, message, null))
        assertFalse(filter(logger, Level.WARN, message, null))
        assertTrue(filter(logger, Level.ERROR, message, null))
        assertFalse(filter(logger, Level.FATAL, message, null))
    }

    @Test
    fun `levelsExcept filter should allow all except specified levels`() {
        val filter = Filter.levelsExcept(Level.DEBUG, Level.WARN)
        assertTrue(filter(logger, Level.TRACE, message, null))
        assertFalse(filter(logger, Level.DEBUG, message, null))
        assertTrue(filter(logger, Level.INFO, message, null))
        assertFalse(filter(logger, Level.WARN, message, null))
        assertTrue(filter(logger, Level.ERROR, message, null))
        assertTrue(filter(logger, Level.FATAL, message, null))
    }

    @Test
    fun `markers filter should only allow specified markers`() {
        val markerA = object : Marker {
            override val key = "A"
            override val name = "A"
            override var isEnabled = true
        }
        val markerB = object : Marker {
            override val key = "B"
            override val name = "B"
            override var isEnabled = true
        }
        val filter = Filter.markers(markerA)

        assertTrue(filter(logger, Level.INFO, message, markerA))
        assertFalse(filter(logger, Level.INFO, message, markerB))
        assertFalse(filter(logger, Level.INFO, message, null))
        assertFalse(filter(logger, Level.INFO, message, NoopMarker))
    }

    @Test
    fun `containsString filter should only allow messages containing the string`() {
        val filter = Filter.containsString("needle")
        assertTrue(filter(logger, Level.INFO, "search for needle in haystack", null))
        assertFalse(filter(logger, Level.INFO, "just a haystack", null))
    }

    @Test
    fun `and filter should combine two filters with logical AND`() {
        val filter1 = Filter.levels(Level.INFO)
        val filter2 = Filter.containsString("pass")
        val combined = filter1 and filter2

        assertTrue(combined(logger, Level.INFO, "this should pass", null))
        assertFalse(combined(logger, Level.DEBUG, "this should pass", null))
        assertFalse(combined(logger, Level.INFO, "this should fail", null))
    }

    @Test
    fun `or filter should combine two filters with logical OR`() {
        val filter1 = Filter.levels(Level.ERROR)
        val filter2 = Filter.containsString("important")
        val combined = filter1 or filter2

        assertTrue(combined(logger, Level.ERROR, "any error", null))
        assertTrue(combined(logger, Level.INFO, "important message", null))
        assertFalse(combined(logger, Level.INFO, "regular message", null))
    }

    @Test
    fun `not filter should negate the filter result`() {
        val filter = Filter.levels(Level.INFO)
        val negated = filter.not()

        assertFalse(negated(logger, Level.INFO, message, null))
        assertTrue(negated(logger, Level.DEBUG, message, null))
    }
}
