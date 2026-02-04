package de.connect2x.lognity.api.backend

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackendTest {
    @Suppress("DEPRECATION")
    @Test
    fun `setOnce returns after first call`() {
        Backend.reset()
        var wasInvoked = false
        Backend.setOnce(NoopBackend) {
            wasInvoked = true
        }
        assertTrue(wasInvoked)
        wasInvoked = false
        Backend.setOnce(NoopBackend) {
            wasInvoked = true
        }
        assertFalse(wasInvoked)
    }
}