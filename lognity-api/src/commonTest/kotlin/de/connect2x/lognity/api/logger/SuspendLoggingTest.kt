package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.ansi.AnsiScope
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.backend.NoopBackend
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.marker.Marker
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SuspendLoggingTest {
    private class TestLogger(
        override val context: Context,
    ) : Logger {
        override val config: Config = Config()
        override var level: Level = Level.INFO
        override var isEnabled: Boolean = true

        override fun log(level: Level, message: AnsiScope.() -> Any?) {}
        override fun log(marker: Marker?, level: Level, message: AnsiScope.() -> Any?) {}
    }

    private var lastCreatedLoggerName: String? = null

    @BeforeTest
    fun setUp() {
        lastCreatedLoggerName = null
        // Set up a mock backend that captures the name and context of created loggers.
        Backend.set(object : Backend by NoopBackend {
            override fun createLogger(name: String?, contextSpec: ContextSpec): Logger {
                lastCreatedLoggerName = name
                return TestLogger(Context(contextSpec))
            }
        })
    }

    @Test
    fun `deriveSuspend propagates CoroutineName from coroutine context`() = runTest {
        val logger = TestLogger(Context { value(Logger.Name("original")) })

        // When we are inside a coroutine with a CoroutineName
        withContext(CoroutineName("test-coroutine")) {
            val derived = logger.deriveSuspend()

            // Then that CoroutineName should be automatically added to the logger's context
            val coroutineNameElement = derived.context[Logger.CoroutineName]
            assertNotNull(coroutineNameElement)
            assertEquals("test-coroutine", coroutineNameElement.name)
        }
    }

    @Test
    fun `deriveSuspend does not set CoroutineName if not present in context`() = runTest {
        val logger = TestLogger(Context { value(Logger.Name("original")) })

        // When we call deriveSuspend without a CoroutineName in the current coroutine context
        val derived = logger.deriveSuspend()

        // Then the CoroutineName in the logger's context should match the current context (which might be null or whatever runTest sets)
        val currentName = currentCoroutineContext()[CoroutineName]?.name
        assertEquals(currentName, derived.context[Logger.CoroutineName]?.name)
    }

    @Test
    fun `deriveSuspend propagates logger name`() = runTest {
        val logger = TestLogger(Context { value(Logger.Name("my-logger")) })

        // When we derive a logger
        logger.deriveSuspend()

        // Then the name of the original logger should be passed to the backend when creating the new logger
        assertEquals("my-logger", lastCreatedLoggerName)
    }

    @Test
    fun `deriveSuspend merges existing context`() = runTest {
        val logger = TestLogger(Context {
            value(Logger.Name("original"))
            value(Logger.CoroutineName("old-name"))
        })

        // When we are in a coroutine with a different name
        withContext(CoroutineName("new-name")) {
            val derived = logger.deriveSuspend()

            // Then the new CoroutineName should override the old one, but other context values like Logger.Name should be preserved
            assertEquals("new-name", derived.context[Logger.CoroutineName]?.name)
            assertEquals("original", derived.context[Logger.Name]?.name)
        }
    }

    @Test
    fun `deriveSuspend applies contextSpec`() = runTest {
        val logger = TestLogger(Context { value(Logger.Name("original")) })

        // When we derive with an additional context specification
        val derived = logger.deriveSuspend {
            value(Logger.Name("overridden"))
        }

        // Then that specification should be applied on top of the existing context
        assertEquals("overridden", derived.context[Logger.Name]?.name)
    }
}
