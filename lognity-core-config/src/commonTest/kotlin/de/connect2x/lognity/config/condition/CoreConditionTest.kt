package de.connect2x.lognity.config.condition

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.EmptyContext
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

class CoreConditionTest {
    private class TestLogger(override val context: Context = EmptyContext) : Logger {
        override val config: Config = Config {}
        override var level: Level = Level.FATAL
        override var isEnabled: Boolean = false

        override fun log(level: Level, message: MessageProvider) = error("Unexpected logging")
        override fun log(marker: Marker?, level: Level, message: MessageProvider) = error("Unexpected logging")
    }

    private class TestMarker(override val key: String, override val name: String) : Marker {
        override var isEnabled: Boolean = false
    }

    private data class StringCase(
        val type: StringConditionType,
        val matchingValue: String,
        val nonMatchingValue: String
    )

    private val stringCases = listOf(
        StringCase(StringConditionType.EQUALS, "prefix-middle-suffix", "middle"),
        StringCase(StringConditionType.NOT_EQUALS, "middle", "prefix-middle-suffix"),
        StringCase(StringConditionType.CONTAINS, "middle", "missing"),
        StringCase(StringConditionType.NOT_CONTAINS, "missing", "middle"),
        StringCase(StringConditionType.STARTS_WITH, "prefix", "middle"),
        StringCase(StringConditionType.NOT_STARTS_WITH, "middle", "prefix"),
        StringCase(StringConditionType.ENDS_WITH, "suffix", "middle"),
        StringCase(StringConditionType.NOT_ENDS_WITH, "middle", "suffix")
    )

    private val stringFactories: List<(RefOrValue<StringConditionType>, RefOrValue<String>) -> AbstractSerializableCondition> = listOf(
        { type, value -> MessageCondition(type, value) },
        { type, value -> LoggerNameCondition(type, value) },
        { type, value -> CoroutineNameCondition(type, value) }
    )

    private fun assertStringComparisons(
        logger: Logger,
        message: String,
        factory: (RefOrValue<StringConditionType>, RefOrValue<String>) -> AbstractSerializableCondition
    ) {
        for (case in stringCases) {
            for ((value, expected) in listOf(case.matchingValue to true, case.nonMatchingValue to false)) {
                val condition = factory(RefOrValue.Value(case.type), RefOrValue.Value(value))
                assertEquals(expected, condition(logger, Level.INFO, message, null), "$condition")
            }
        }
    }

    @Test
    fun `Level conditions compare event level rather than logger threshold`() {
        val logger = TestLogger()
        val expected = mapOf(
            LevelCondition.Type.EQUALS to setOf(Level.INFO),
            LevelCondition.Type.NOT_EQUALS to setOf(Level.TRACE, Level.DEBUG, Level.WARN, Level.ERROR, Level.FATAL),
            LevelCondition.Type.BELOW to setOf(Level.TRACE, Level.DEBUG),
            LevelCondition.Type.ABOVE to setOf(Level.WARN, Level.ERROR, Level.FATAL)
        )

        for ((type, matchingLevels) in expected) {
            val condition = LevelCondition(RefOrValue.Value(type), RefOrValue.Value(Level.INFO))
            for (level in Level.entries) {
                assertEquals(level in matchingLevels, condition(logger, level, "message", null), "$type: $level")
            }
        }
        for (level in Level.entries) {
            val below = LevelCondition(RefOrValue.Value(LevelCondition.Type.BELOW), RefOrValue.Value(level))
            val above = LevelCondition(RefOrValue.Value(LevelCondition.Type.ABOVE), RefOrValue.Value(level))
            assertFalse(below(logger, level, "message", null))
            assertFalse(above(logger, level, "message", null))
        }
    }

    @Test
    fun `Message conditions apply every string comparison to the message`() {
        assertStringComparisons(TestLogger(), "prefix-middle-suffix") { type, value -> MessageCondition(type, value) }
    }

    @Test
    fun `Logger name conditions use the logger name rather than coroutine name or message`() {
        val logger = TestLogger(Context {
            value(Logger.Name("prefix-middle-suffix"))
            value(Logger.CoroutineName("unrelated"))
        })

        assertStringComparisons(logger, "unrelated") { type, value -> LoggerNameCondition(type, value) }
    }

    @Test
    fun `Coroutine name conditions use the coroutine name rather than logger name or message`() {
        val logger = TestLogger(Context {
            value(Logger.Name("unrelated"))
            value(Logger.CoroutineName("prefix-middle-suffix"))
        })

        assertStringComparisons(logger, "unrelated") { type, value -> CoroutineNameCondition(type, value) }
    }

    @Test
    fun `Missing context names never match including negated comparisons`() {
        val withoutLoggerName = TestLogger(Context { value(Logger.CoroutineName("value")) })
        val withoutCoroutineName = TestLogger(Context { value(Logger.Name("value")) })

        for (type in StringConditionType.entries) {
            val loggerName = LoggerNameCondition(RefOrValue.Value(type), RefOrValue.Value("value"))
            val coroutineName = CoroutineNameCondition(RefOrValue.Value(type), RefOrValue.Value("value"))
            assertFalse(loggerName(withoutLoggerName, Level.INFO, "value", null), "$type")
            assertFalse(coroutineName(withoutCoroutineName, Level.INFO, "value", null), "$type")
        }
    }

    @Test
    fun `String conditions distinguish empty strings from missing context and are case sensitive`() {
        val positiveTypes = setOf(
            StringConditionType.EQUALS, StringConditionType.CONTAINS,
            StringConditionType.STARTS_WITH, StringConditionType.ENDS_WITH
        )
        for (input in listOf("", "VALUE")) {
            val logger = TestLogger(Context {
                value(Logger.Name(input))
                value(Logger.CoroutineName(input))
            })
            for (type in StringConditionType.entries) {
                for (factory in stringFactories) {
                    val value = if (input.isEmpty()) "" else "value"
                    val condition = factory(RefOrValue.Value(type), RefOrValue.Value(value))
                    val expected = if (input.isEmpty()) type in positiveTypes else type !in positiveTypes
                    assertEquals(expected, condition(logger, Level.INFO, input, null), "$condition: '$input'")
                }
            }
        }
    }

    @Test
    fun `Marker comparisons distinguish keys from display names`() {
        val types = mapOf(
            StringConditionType.EQUALS to (MarkerConditionType.KEY_EQUALS to MarkerConditionType.NAME_EQUALS),
            StringConditionType.NOT_EQUALS to (MarkerConditionType.KEY_NOT_EQUALS to MarkerConditionType.NAME_NOT_EQUALS),
            StringConditionType.CONTAINS to (MarkerConditionType.KEY_CONTAINS to MarkerConditionType.NAME_CONTAINS),
            StringConditionType.NOT_CONTAINS to (MarkerConditionType.KEY_NOT_CONTAINS to MarkerConditionType.NAME_NOT_CONTAINS),
            StringConditionType.STARTS_WITH to (MarkerConditionType.KEY_STARTS_WITH to MarkerConditionType.NAME_STARTS_WITH),
            StringConditionType.NOT_STARTS_WITH to (MarkerConditionType.KEY_NOT_STARTS_WITH to MarkerConditionType.NAME_NOT_STARTS_WITH),
            StringConditionType.ENDS_WITH to (MarkerConditionType.KEY_ENDS_WITH to MarkerConditionType.NAME_ENDS_WITH),
            StringConditionType.NOT_ENDS_WITH to (MarkerConditionType.KEY_NOT_ENDS_WITH to MarkerConditionType.NAME_NOT_ENDS_WITH)
        )
        val logger = TestLogger()
        val keyMarker = TestMarker("prefix-middle-suffix", "unrelated")
        val nameMarker = TestMarker("unrelated", "prefix-middle-suffix")

        for (case in stringCases) {
            val (keyType, nameType) = types.getValue(case.type)
            for ((value, expected) in listOf(case.matchingValue to true, case.nonMatchingValue to false)) {
                val keyCondition = MarkerCondition(RefOrValue.Value(keyType), RefOrValue.Value(value))
                val nameCondition = MarkerCondition(RefOrValue.Value(nameType), RefOrValue.Value(value))
                assertEquals(expected, keyCondition(logger, Level.INFO, "message", keyMarker), "$keyCondition")
                assertEquals(expected, nameCondition(logger, Level.INFO, "message", nameMarker), "$nameCondition")
            }
        }
    }

    @Test
    fun `Null markers follow each comparison null semantics`() {
        val matchingTypes = setOf(
            MarkerConditionType.KEY_NOT_EQUALS, MarkerConditionType.NAME_NOT_EQUALS,
            MarkerConditionType.KEY_NOT_STARTS_WITH, MarkerConditionType.NAME_NOT_STARTS_WITH,
            MarkerConditionType.KEY_NOT_ENDS_WITH, MarkerConditionType.NAME_NOT_ENDS_WITH
        )
        val logger = TestLogger()

        for (type in MarkerConditionType.entries) {
            for (value in listOf("", "value")) {
                val condition = MarkerCondition(RefOrValue.Value(type), RefOrValue.Value(value))
                assertEquals(type in matchingTypes, condition(logger, Level.INFO, "message", null), "$condition")
            }
        }
    }

    @Test
    fun `Leaf conditions reject unresolved operation and value references`() {
        val missing = "missing-condition-test-provider"
        val operation = RefOrValue.Ref<Nothing>(missing)
        val value = RefOrValue.Ref<Nothing>(missing)
        val conditions = stringFactories.flatMap { factory ->
            listOf(
                factory(operation, RefOrValue.Value("value")),
                factory(RefOrValue.Value(StringConditionType.EQUALS), value)
            )
        } + listOf(
            LevelCondition(operation, RefOrValue.Value(Level.INFO)),
            LevelCondition(RefOrValue.Value(LevelCondition.Type.EQUALS), value),
            MarkerCondition(operation, RefOrValue.Value("value")),
            MarkerCondition(RefOrValue.Value(MarkerConditionType.KEY_EQUALS), value)
        )
        val logger = TestLogger(Context {
            value(Logger.Name("value"))
            value(Logger.CoroutineName("value"))
        })

        for (condition in conditions) {
            val exception = assertFailsWith<IllegalStateException> {
                condition(logger, Level.INFO, "value", TestMarker("value", "value"))
            }
            assertEquals("Could not resolve provider for config reference '$missing'", exception.message)
        }
    }

    @Test
    fun `Leaf conditions inherit replaceable config and default to unnamed`() {
        val conditions = stringFactories.map { it(RefOrValue.Value(StringConditionType.EQUALS), RefOrValue.Value("value")) } + listOf(
            LevelCondition(RefOrValue.Value(LevelCondition.Type.EQUALS), RefOrValue.Value(Level.INFO)),
            MarkerCondition(RefOrValue.Value(MarkerConditionType.KEY_EQUALS), RefOrValue.Value("value"))
        )
        val first = SerializableConfig()
        val second = SerializableConfig()

        for (condition in conditions) {
            assertNull(condition.name.resolve())
            condition.config = first
            assertSame(first, condition.config)
            condition.config = second
            assertSame(second, condition.config)
        }
    }

    private fun <T : AbstractSerializableCondition> assertRoundTrip(condition: T, serializer: KSerializer<T>) {
        condition.config = SerializableConfig()
        val encoded = Json.encodeToString(serializer, condition)
        assertEquals(setOf("condition", "value", "name"), Json.parseToJsonElement(encoded).jsonObject.keys)
        val decoded = Json.decodeFromString(serializer, encoded)
        assertEquals(condition, decoded)
        val exception = assertFails { decoded.config }
        assertEquals("lateinit property _config has not been initialized", exception.message)
    }

    @Test
    fun `Leaf condition serialization retains constructor properties but not config`() {
        val type = RefOrValue.Value(StringConditionType.CONTAINS)
        val value = RefOrValue.Value("value")
        val name = RefOrValue.Value("named")

        assertRoundTrip(MessageCondition(type, value, name), MessageCondition.serializer())
        assertRoundTrip(LoggerNameCondition(type, value, name), LoggerNameCondition.serializer())
        assertRoundTrip(CoroutineNameCondition(type, value, name), CoroutineNameCondition.serializer())
        assertRoundTrip(
            LevelCondition(RefOrValue.Value(LevelCondition.Type.ABOVE), RefOrValue.Value(Level.WARN), name),
            LevelCondition.serializer()
        )
        assertRoundTrip(
            MarkerCondition(RefOrValue.Value(MarkerConditionType.NAME_CONTAINS), value, name),
            MarkerCondition.serializer()
        )
    }
}