package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CompositeConditionTest {
    private val logger = ConditionTestLogger()

    private fun composites(conditions: List<RefOrValue<SerializableCondition>>): List<AbstractSerializableCondition> {
        return listOf(AndCondition(conditions), OrCondition(conditions), ExactlyOneCondition(conditions))
    }

    private fun assertResults(results: List<Boolean>, and: Boolean, or: Boolean, exactlyOne: Boolean) {
        val children = results.map { RefOrValue.Value(RecordingCondition(it)) }
        val conditions = composites(children)
        val expected = listOf(and, or, exactlyOne)

        for ((index, condition) in conditions.withIndex()) {
            condition.config = SerializableConfig()
            assertEquals(expected[index], condition(logger, Level.INFO, "message", null), "$condition: $results")
        }
    }

    @Test
    fun `Composites evaluate truth tables including empty and single child lists`() {
        assertResults(emptyList(), and = true, or = false, exactlyOne = false)
        assertResults(listOf(false), and = false, or = false, exactlyOne = false)
        assertResults(listOf(true), and = true, or = true, exactlyOne = true)
        assertResults(listOf(false, false), and = false, or = false, exactlyOne = false)
        assertResults(listOf(false, true), and = false, or = true, exactlyOne = true)
        assertResults(listOf(true, false), and = false, or = true, exactlyOne = true)
        assertResults(listOf(true, true), and = true, or = true, exactlyOne = false)
        assertResults(listOf(false, false, false), and = false, or = false, exactlyOne = false)
        assertResults(listOf(false, false, true), and = false, or = true, exactlyOne = true)
        assertResults(listOf(false, true, false), and = false, or = true, exactlyOne = true)
        assertResults(listOf(true, false, false), and = false, or = true, exactlyOne = true)
        assertResults(listOf(false, true, true), and = false, or = true, exactlyOne = false)
        assertResults(listOf(true, false, true), and = false, or = true, exactlyOne = false)
        assertResults(listOf(true, true, false), and = false, or = true, exactlyOne = false)
        assertResults(listOf(true, true, true), and = true, or = true, exactlyOne = false)
    }

    @Test
    fun `And stops evaluating after the first false result`() {
        val children = listOf(RecordingCondition(true), RecordingCondition(false), RecordingCondition(true))
        val condition = AndCondition(children.map { RefOrValue.Value(it) })
        condition.config = SerializableConfig()

        assertFalse(condition(logger, Level.INFO, "message", null))
        assertEquals(listOf(1, 1, 0), children.map { it.invocations.size })
    }

    @Test
    fun `Or stops evaluating after the first true result`() {
        val children = listOf(RecordingCondition(false), RecordingCondition(true), RecordingCondition(false))
        val condition = OrCondition(children.map { RefOrValue.Value(it) })
        condition.config = SerializableConfig()

        assertTrue(condition(logger, Level.INFO, "message", null))
        assertEquals(listOf(1, 1, 0), children.map { it.invocations.size })
    }

    @Test
    fun `Exactly one evaluates every child once`() {
        val children = listOf(RecordingCondition(true), RecordingCondition(true), RecordingCondition(false))
        val condition = ExactlyOneCondition(children.map { RefOrValue.Value(it) })
        condition.config = SerializableConfig()

        assertFalse(condition(logger, Level.INFO, "message", null))
        assertEquals(listOf(1, 1, 1), children.map { it.invocations.size })
    }

    @Test
    fun `Composites forward all invocation arguments unchanged`() {
        val marker = object : Marker {
            override val key: String = "key"
            override val name: String = "name"
            override var isEnabled: Boolean = true
        }
        val child = RecordingCondition(true)

        for (condition in composites(listOf(RefOrValue.Value(child)))) {
            condition.config = SerializableConfig()
            for (argument in listOf(marker, null)) {
                child.invocations.clear()
                assertTrue(condition(logger, Level.ERROR, "original message", argument))
                val invocation = child.invocations.single()
                assertSame(logger, invocation.logger)
                assertEquals(Level.ERROR, invocation.level)
                assertEquals("original message", invocation.message)
                assertSame(argument, invocation.marker)
            }
        }
    }

    @Test
    fun `Config propagates to all direct and referenced children even when evaluation short circuits`() {
        val direct = RecordingCondition(false)
        val referenced = RecordingCondition(true, RefOrValue.Value("referenced"))
        val children = listOf(
            RefOrValue.Value(direct),
            RefOrValue.TemplateRef<SerializableCondition>("conditions", "referenced")
        )

        for (condition in composites(children)) {
            val first = SerializableConfig(conditions = listOf(referenced))
            val second = SerializableConfig(conditions = listOf(referenced))
            for (config in listOf(first, second)) {
                condition.config = config
                assertSame(config, condition.config)
                assertSame(config, direct.config)
                assertSame(config, referenced.config)
            }
            assertTrue(direct.invocations.isEmpty())
            assertTrue(referenced.invocations.isEmpty())
        }
    }

    @Test
    fun `Config propagates recursively through mixed nested composites`() {
        val leaf = RecordingCondition(true, RefOrValue.Value("leaf"))
        val inner = ExactlyOneCondition(listOf(RefOrValue.TemplateRef("conditions", "leaf")))
        val middle = OrCondition(listOf(RefOrValue.Value(inner)), RefOrValue.Value("middle"))
        val outer = AndCondition(listOf(RefOrValue.TemplateRef("conditions", "middle")))

        val first = SerializableConfig(conditions = listOf(middle, leaf))
        val second = SerializableConfig(conditions = listOf(middle, leaf))
        for (config in listOf(first, second)) {
            outer.config = config
            for (condition in listOf(outer, middle, inner, leaf)) {
                assertSame(config, condition.config)
            }
            assertTrue(outer(logger, Level.INFO, "message", null))
        }
    }

    @Test
    fun `Reassigning config resolves references against the new config`() {
        val firstChild = RecordingCondition(true, RefOrValue.Value("child"))
        val secondChild = RecordingCondition(false, RefOrValue.Value("child"))

        for (condition in composites(listOf(RefOrValue.TemplateRef("conditions", "child")))) {
            val first = SerializableConfig(conditions = listOf(firstChild))
            condition.config = first
            assertSame(first, firstChild.config)
            assertTrue(condition(logger, Level.INFO, "message", null))

            val second = SerializableConfig(conditions = listOf(secondChild))
            condition.config = second
            assertSame(second, secondChild.config)
            assertSame(first, firstChild.config)
            assertFalse(condition(logger, Level.INFO, "message", null))
        }
    }

    @Test
    fun `Non abstract child conditions are evaluated without receiving config`() {
        val child = object : SerializableCondition {
            override var config: SerializableConfig
                get() = error("Config must not be read")
                set(value) = error("Config must not be assigned")
            override val name: RefOrValue<String?> = RefOrValue.Value(null)
            override fun invoke(logger: Logger, level: Level, message: String, marker: Marker?): Boolean = true
        }

        for (condition in composites(listOf(RefOrValue.Value(child)))) {
            condition.config = SerializableConfig()
            assertTrue(condition(logger, Level.INFO, "message", null))
        }
    }

    @Test
    fun `Uninitialized composite config cannot be read or used to resolve children`() {
        for (condition in composites(listOf(RefOrValue.Value(RecordingCondition(true))))) {
            val readException = assertFails { condition.config }
            val invokeException = assertFails { condition(logger, Level.INFO, "message", null) }
            assertEquals("lateinit property _config has not been initialized", readException.message)
            assertEquals("lateinit property _config has not been initialized", invokeException.message)
        }
    }

    @Test
    fun `Missing condition references fail when config is assigned`() {
        for (condition in composites(listOf(RefOrValue.TemplateRef("conditions", "missing")))) {
            val exception = assertFailsWith<IllegalStateException> { condition.config = SerializableConfig() }
            assertEquals("Could not find pre-defined condition named 'missing'", exception.message)
        }
    }

    @Test
    fun `Composite names default to null and preserve explicit names`() {
        for (condition in composites(emptyList())) {
            assertNull(condition.name.resolve())
        }
        val name = RefOrValue.Value("named")
        val named = listOf(
            AndCondition(emptyList(), name),
            OrCondition(emptyList(), name),
            ExactlyOneCondition(emptyList(), name)
        )
        for (condition in named) {
            assertEquals("named", condition.name.resolve())
        }
    }

    @Test
    fun `Composite serialization omits config and retains constructor properties`() {
        val config = SerializableConfig()
        val and = AndCondition(emptyList(), RefOrValue.Value("and"))
        val or = OrCondition(emptyList(), RefOrValue.Value("or"))
        val exactlyOne = ExactlyOneCondition(emptyList(), RefOrValue.Value("one"))
        for (condition in listOf(and, or, exactlyOne)) {
            condition.config = config
        }

        val encodedAnd = Json.encodeToString(AndCondition.serializer(), and)
        val encodedOr = Json.encodeToString(OrCondition.serializer(), or)
        val encodedOne = Json.encodeToString(ExactlyOneCondition.serializer(), exactlyOne)
        assertEquals("{\"conditions\":[],\"name\":\"and\"}", encodedAnd)
        assertEquals("{\"conditions\":[],\"name\":\"or\"}", encodedOr)
        assertEquals("{\"conditions\":[],\"name\":\"one\"}", encodedOne)

        val decoded = listOf(
            Json.decodeFromString(AndCondition.serializer(), encodedAnd),
            Json.decodeFromString(OrCondition.serializer(), encodedOr),
            Json.decodeFromString(ExactlyOneCondition.serializer(), encodedOne)
        )
        assertEquals(listOf(and, or, exactlyOne), decoded)
        for (condition in decoded) {
            val exception = assertFails { condition.config }
            assertEquals("lateinit property _config has not been initialized", exception.message)
        }
    }

    @Test
    fun `Nested built in conditions loaded from JSON receive config and evaluate`() {
        val source = Buffer()
        source.writeString(
            """
            {
              "conditions": [{
                "type": "and",
                "conditions": [{
                  "type": "or",
                  "conditions": [{
                    "type": "exactly_one",
                    "conditions": [{"type": "always"}]
                  }]
                }]
              }]
            }
            """.trimIndent()
        )
        val config = SerializableConfig.load(source)
        val condition = config.conditions.single()
        condition.config = config

        assertTrue(condition(logger, Level.INFO, "message", null))
    }
}