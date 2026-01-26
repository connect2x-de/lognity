package de.connect2x.lognity.api.backend

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackendTest {
    private fun assertThrows(block: () -> Unit) {
        var hasThrown = false
        try {
            block()
        } catch (_: Throwable) {
            hasThrown = true
        }
        assertTrue(hasThrown)
    }

    @Test
    fun `set throws after first call`() {
        Backend.reset()
        Backend.set(NoopBackend)
        assertThrows {
            Backend.set(NoopBackend)
        }
    }

    @Test
    fun `set throws after first mutating access`() {
        Backend.reset()
        Backend.configSpec = {}
        assertThrows {
            Backend.set(NoopBackend)
        }
    }

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