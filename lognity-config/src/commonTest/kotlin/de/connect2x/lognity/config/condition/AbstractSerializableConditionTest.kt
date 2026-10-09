package de.connect2x.lognity.config.condition

import de.connect2x.lognity.config.SerializableConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

class AbstractSerializableConditionTest {
    @Test
    fun `Reading config before initialization fails`() {
        val condition = RecordingCondition(true)

        val exception = assertFails { condition.config }
        assertEquals("lateinit property _config has not been initialized", exception.message)
    }

    @Test
    fun `Config retains the assigned instance and can be replaced`() {
        val condition = RecordingCondition(true)
        val first = SerializableConfig()
        val second = SerializableConfig()

        condition.config = first
        assertSame(first, condition.config)

        condition.config = second
        assertSame(second, condition.config)
    }
}