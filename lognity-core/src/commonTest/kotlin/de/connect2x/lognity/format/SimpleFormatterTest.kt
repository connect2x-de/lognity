package de.connect2x.lognity.format

import de.connect2x.lognity.api.ansi.AnsiSequence
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.NoopLogger
import de.connect2x.lognity.api.marker.Marker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SimpleFormatterTest {
    private val timestamp = Instant.parse("2024-03-19T14:30:00.123Z")

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
            formatter(logger, Level.INFO, "msg", null, timestamp, "INFO{{r}}"),
        )

        // marker
        assertEquals("test-marker", formatter(logger, Level.INFO, "msg", TestMarker(), timestamp, "{{marker}}"))
        assertEquals("(_)", formatter(logger, Level.INFO, "msg", null, timestamp, "{{marker}}"))

        // message
        assertEquals("Hello World", formatter(logger, Level.INFO, "Hello World", null, timestamp, "{{message}}"))
        assertEquals("123", formatter(logger, Level.INFO, 123, null, timestamp, "{{message}}"))

        // thread and threadId (just check they are not empty)
        assertTrue(formatter(logger, Level.INFO, "msg", null, timestamp, "{{thread}}").isNotEmpty())

        // level (padded)
        assertEquals("INFO-", formatter(logger, Level.INFO, "msg", null, timestamp, "{{level}}"))
        assertEquals("DEBUG", formatter(logger, Level.DEBUG, "msg", null, timestamp, "{{level}}"))

        // levelSymbol
        assertEquals(Level.INFO.symbol, formatter(logger, Level.INFO, "msg", null, timestamp, "{{levelSymbol}}"))

        // name
        assertEquals("my-logger", formatter(logger, Level.INFO, "msg", null, timestamp, "{{name}}"))
        assertEquals("(_)", formatter(NoopLogger, Level.INFO, "msg", null, timestamp, "{{name}}"))

        // coroutineName
        assertEquals("my-coroutine", formatter(logger, Level.INFO, "msg", null, timestamp, "{{coroutineName}}"))
        assertEquals("(_)", formatter(NoopLogger, Level.INFO, "msg", null, timestamp, "{{coroutineName}}"))

        // timestamp components (2024-03-19T14:30:00.123Z)
        assertEquals("2024", formatter(logger, Level.INFO, "msg", null, timestamp, "{{yyyy}}"))
        assertEquals("03", formatter(logger, Level.INFO, "msg", null, timestamp, "{{MM}}"))
        assertEquals("19", formatter(logger, Level.INFO, "msg", null, timestamp, "{{dd}}"))
        assertEquals("14", formatter(logger, Level.INFO, "msg", null, timestamp, "{{hh}}"))
        assertEquals("30", formatter(logger, Level.INFO, "msg", null, timestamp, "{{mm}}"))
        assertEquals("00", formatter(logger, Level.INFO, "msg", null, timestamp, "{{ss}}"))
        assertEquals("123", formatter(logger, Level.INFO, "msg", null, timestamp, "{{SSS}}"))
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
            formatter(NoopLogger, Level.INFO, "hello", null, timestamp, "{{custom}} {{static}}"),
        )
    }

    @Test
    fun `Compilation and caching`() {
        val formatter = SimpleFormatter(
            mapOf("v" to CompiledFormat.Variable { ctx -> ctx.content.toString() }),
        )

        val formatString = "Value: {{v}}"

        // First call triggers compilation
        assertEquals("Value: foo", formatter(NoopLogger, Level.INFO, "foo", null, timestamp, formatString))

        // Second call should use cached format
        assertEquals("Value: bar", formatter(NoopLogger, Level.INFO, "bar", null, timestamp, formatString))
    }

    @Test
    fun `Text only format`() {
        val formatter = SimpleFormatter(emptyMap())
        assertEquals("pure text", formatter(NoopLogger, Level.INFO, "msg", null, timestamp, "pure text"))
    }

    @Test
    fun `Missing variable does not throw and renders as literal text`() {
        val formatter = SimpleFormatter(emptyMap())
        // Unknown variables are currently not recognized by the DFA if not in 'variables' map,
        // so they are treated as plain text.
        assertEquals("{{missing}}", formatter(NoopLogger, Level.INFO, "msg", null, timestamp, "{{missing}}"))
    }
}
