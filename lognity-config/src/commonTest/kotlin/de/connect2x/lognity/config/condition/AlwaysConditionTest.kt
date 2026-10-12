package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.marker.Marker
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AlwaysConditionTest {
    @Test
    fun `Always matches regardless of log arguments`() {
        val logger = ConditionTestLogger()
        val marker = object : Marker {
            override val key: String = "test-key"
            override val name: String = "test-name"
            override var isEnabled: Boolean = false
        }

        for (level in Level.entries) {
            for (message in listOf("", "message")) {
                assertTrue(AlwaysCondition(logger, level, message, null))
                assertTrue(AlwaysCondition(logger, level, message, marker))
            }
        }
        assertEquals("always", AlwaysCondition.name.resolve())
    }

    @Test
    fun `Serialization preserves singleton and omits transient state`() {
        val encoded = Json.encodeToString(AlwaysCondition.serializer(), AlwaysCondition)

        assertEquals("{}", encoded)
        assertSame(AlwaysCondition, Json.decodeFromString(AlwaysCondition.serializer(), encoded))
    }
}