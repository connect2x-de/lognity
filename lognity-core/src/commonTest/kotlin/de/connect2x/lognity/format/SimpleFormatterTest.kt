package de.connect2x.lognity.format

import de.connect2x.lognity.api.ansi.AnsiSequence
import de.connect2x.lognity.api.appender.FakeAppender
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.NoopLogger
import de.connect2x.lognity.api.marker.Marker
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SimpleFormatterTest {
    private val timestamp = Instant.parse("2024-03-19T14:30:00.123Z").toLocalDateTime(TimeZone.UTC)

    private fun createTestLogger(name: String? = null, coroutineName: String? = null): Logger {
        val context = Context {
            if (name != null) {
                this += Logger.Name(name)
            }
            if (coroutineName != null) {
                this += Logger.CoroutineName(coroutineName)
            }
        }

        return object : Logger by NoopLogger {
            override val context: Context = context
        }
    }

    private class TestMarker : Marker {
        override val key: String = "test-key"
        override val name: String = "test-marker"
        override var isEnabled: Boolean = true
    }

    @Test
    fun `Default variables`() {
        val formatter = SimpleFormatter.default
        val logger = createTestLogger(name = "my-logger", coroutineName = "my-coroutine")

        // levelColor and r
        val reset = AnsiSequence.reset.toString()
        assertEquals(
            "INFO${reset}",
            formatter(logger, FakeAppender(pattern = "INFO{{r}}"), Level.INFO, "msg", null, timestamp),
        )

        // marker
        assertEquals(
            "test-marker",
            formatter(logger, FakeAppender(pattern = "{{marker}}"), Level.INFO, "msg", TestMarker(), timestamp)
        )
        assertEquals("(_)", formatter(logger, FakeAppender(pattern = "{{marker}}"), Level.INFO, "msg", null, timestamp))

        // message
        assertEquals(
            "Hello World",
            formatter(logger, FakeAppender(pattern = "{{message}}"), Level.INFO, "Hello World", null, timestamp)
        )
        assertEquals("123", formatter(logger, FakeAppender(pattern = "{{message}}"), Level.INFO, 123, null, timestamp))

        // thread and threadId (just check they are not empty)
        assertTrue(
            formatter(
                logger,
                FakeAppender(pattern = "{{thread}}"),
                Level.INFO,
                "msg",
                null,
                timestamp
            ).isNotEmpty()
        )

        // level (padded)
        assertEquals(
            "INFO-",
            formatter(logger, FakeAppender(pattern = "{{level}}"), Level.INFO, "msg", null, timestamp)
        )
        assertEquals(
            "DEBUG",
            formatter(logger, FakeAppender(pattern = "{{level}}"), Level.DEBUG, "msg", null, timestamp)
        )

        // levelSymbol
        assertEquals(
            Level.INFO.symbol,
            formatter(logger, FakeAppender(pattern = "{{levelSymbol}}"), Level.INFO, "msg", null, timestamp)
        )

        // name
        assertEquals(
            "my-logger",
            formatter(logger, FakeAppender(pattern = "{{name}}"), Level.INFO, "msg", null, timestamp)
        )
        assertEquals(
            "(_)",
            formatter(NoopLogger, FakeAppender(pattern = "{{name}}"), Level.INFO, "msg", null, timestamp)
        )

        // coroutineName
        assertEquals(
            "my-coroutine",
            formatter(logger, FakeAppender(pattern = "{{coroutineName}}"), Level.INFO, "msg", null, timestamp)
        )
        assertEquals(
            "(_)",
            formatter(NoopLogger, FakeAppender(pattern = "{{coroutineName}}"), Level.INFO, "msg", null, timestamp)
        )

        // timestamp components (2024-03-19T14:30:00.123Z)
        assertEquals("2024", formatter(logger, FakeAppender(pattern = "{{yyyy}}"), Level.INFO, "msg", null, timestamp))
        assertEquals("03", formatter(logger, FakeAppender(pattern = "{{MM}}"), Level.INFO, "msg", null, timestamp))
        assertEquals("19", formatter(logger, FakeAppender(pattern = "{{dd}}"), Level.INFO, "msg", null, timestamp))
        assertEquals("14", formatter(logger, FakeAppender(pattern = "{{hh}}"), Level.INFO, "msg", null, timestamp))
        assertEquals("30", formatter(logger, FakeAppender(pattern = "{{mm}}"), Level.INFO, "msg", null, timestamp))
        assertEquals("00", formatter(logger, FakeAppender(pattern = "{{ss}}"), Level.INFO, "msg", null, timestamp))
        assertEquals("123", formatter(logger, FakeAppender(pattern = "{{SSS}}"), Level.INFO, "msg", null, timestamp))
    }

    @Test
    fun `Custom variables`() {
        val variables = mapOf(
            "custom" to CompiledFormat.Variable<FormatterContext> { ctx -> "custom-${ctx.content}" },
            "static" to CompiledFormat.Text("static-val"),
        )
        val formatter = SimpleFormatter(variables)

        assertEquals(
            "custom-hello static-val",
            formatter(
                NoopLogger,
                FakeAppender(pattern = "{{custom}} {{static}}"),
                Level.INFO,
                "hello",
                null,
                timestamp
            ),
        )
    }

    @Test
    fun `Compilation and caching`() {
        val formatter = SimpleFormatter(
            mapOf("v" to CompiledFormat.Variable { ctx -> ctx.content.toString() }),
        )

        val formatString = "Value: {{v}}"

        // First call triggers compilation
        assertEquals(
            "Value: foo",
            formatter(NoopLogger, FakeAppender(pattern = formatString), Level.INFO, "foo", null, timestamp)
        )

        // Second call should use cached format
        assertEquals(
            "Value: bar",
            formatter(NoopLogger, FakeAppender(pattern = formatString), Level.INFO, "bar", null, timestamp)
        )
    }

    @Test
    fun `Text only format`() {
        val formatter = SimpleFormatter(emptyMap())
        assertEquals(
            "pure text",
            formatter(NoopLogger, FakeAppender(pattern = "pure text"), Level.INFO, "msg", null, timestamp)
        )
    }

    @Test
    fun `Missing variable does not throw and renders as literal text`() {
        val formatter = SimpleFormatter(emptyMap())
        // Unknown variables are currently not recognized by the DFA if not in 'variables' map,
        // so they are treated as plain text.
        assertEquals(
            "{{missing}}",
            formatter(NoopLogger, FakeAppender(pattern = "{{missing}}"), Level.INFO, "msg", null, timestamp)
        )
    }
}
