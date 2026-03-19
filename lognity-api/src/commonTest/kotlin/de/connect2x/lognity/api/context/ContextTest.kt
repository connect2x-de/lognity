package de.connect2x.lognity.api.context

import de.connect2x.lognity.api.context.Context.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ContextTest {
    private object TestKey1 : Key<TestElement1>

    private class TestElement1 : Context.Element {
        override val key: Key<*> = TestKey1
    }

    private object TestKey2 : Key<TestElement2>

    private class TestElement2 : Context.Element {
        override val key: Key<*> = TestKey2
    }

    @Test
    fun `EmptyContext elements is empty`() {
        assertTrue(EmptyContext.elements.isEmpty())
    }

    @Test
    fun `EmptyContext get returns null`() {
        assertNull(EmptyContext[TestKey1])
    }

    @Test
    fun `EmptyContext plus returns other context`() {
        val element = TestElement1()
        val otherContext = Context {
            this += element
        }
        assertSame(otherContext, EmptyContext + otherContext)
    }

    @Test
    fun `Context builder creates context with elements`() {
        val e1 = TestElement1()
        val e2 = TestElement2()
        val context = Context {
            this += e1
            this += e2
        }

        assertEquals(2, context.elements.size)
        assertEquals(e1, context[TestKey1])
        assertEquals(e2, context[TestKey2])
    }

    @Test
    fun `Context builder valuesFrom copies all elements from other context`() {
        val e1 = TestElement1()
        val otherCtx = Context { this += e1 }
        val context = Context {
            valuesFrom(otherCtx)
        }

        assertEquals(e1, context[TestKey1])
    }

    @Test
    fun `Context builder values call with Map adds elements`() {
        val e1 = TestElement1()
        val context = Context {
            values(mapOf(TestKey1 to e1))
        }

        assertEquals(e1, context[TestKey1])
    }

    @Test
    fun `Context builder values call with Iterable adds elements`() {
        val e1 = TestElement1()
        val context = Context {
            values(listOf(e1))
        }

        assertEquals(e1, context[TestKey1])
    }

    @Test
    fun `Context builder plusAssign with map adds elements`() {
        val e1 = TestElement1()
        val context = Context {
            this += mapOf(TestKey1 to e1)
        }

        assertEquals(e1, context[TestKey1])
    }

    @Test
    fun `Context builder plusAssign with iterable adds elements`() {
        val e1 = TestElement1()
        val context = Context {
            this += listOf(e1)
        }

        assertEquals(e1, context[TestKey1])
    }

    @Test
    fun `Context get returns null for missing key`() {
        val context = Context {
            this += TestElement1()
        }
        assertNull(context[TestKey2])
    }

    @Test
    fun `Context plus merges elements from both contexts`() {
        val e1 = TestElement1()
        val e2 = TestElement2()

        val ctx1 = Context { this += e1 }
        val ctx2 = Context { this += e2 }

        val combined = ctx1 + ctx2

        assertEquals(2, combined.elements.size)
        assertEquals(e1, combined[TestKey1])
        assertEquals(e2, combined[TestKey2])
    }

    @Test
    fun `Context plus overwrites elements with same key from right side`() {
        val e1a = TestElement1()
        val e1b = TestElement1()

        val ctx1 = Context { this += e1a }
        val ctx2 = Context { this += e1b }

        val combined = ctx1 + ctx2

        assertEquals(1, combined.elements.size)
        assertEquals(e1b, combined[TestKey1])
    }

    @Test
    fun `Context plus with EmptyContext returns equal elements`() {
        val e1 = TestElement1()
        val ctx1 = Context { this += e1 }

        val combined = ctx1 + EmptyContext

        assertEquals(ctx1.elements, combined.elements)
    }

    @Test
    fun `Context plus with same elements de-duplicates`() {
        val e1 = TestElement1()
        val ctx1 = Context { this += e1 }
        val ctx2 = Context { this += e1 }

        val combined = ctx1 + ctx2

        assertEquals(1, combined.elements.size)
        assertEquals(e1, combined[TestKey1])
    }
}
