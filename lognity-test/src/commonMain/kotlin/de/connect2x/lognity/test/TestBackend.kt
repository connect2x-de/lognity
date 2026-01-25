package de.connect2x.lognity.test

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.config.ConfigSpec
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.ContextBuilder
import de.connect2x.lognity.api.context.ContextSpec
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.Logger.Name
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.systemConsoleAppender
import de.connect2x.lognity.test.TestBackend.testScope
import kotlinx.coroutines.test.TestScope
import org.jetbrains.annotations.TestOnly
import kotlin.concurrent.atomics.AtomicReference

/**
 * A specialized [Backend] for testing purposes.
 *
 * This backend provides utilities to easily configure and use Lognity in tests,
 * including support for [TestScope] and temporary configuration changes.
 */
@TestOnly
object TestBackend : Backend by DefaultBackend {
    @PublishedApi
    internal val _testScope: AtomicReference<TestScope?> = AtomicReference(null)

    /**
     * The current [TestScope] used by the backend.
     */
    inline var testScope: TestScope?
        get() = _testScope.load()
        set(value) {
            _testScope.store(value)
        }

    init { // Increase to debug level for tests by default
        configSpec = {
            systemConsoleAppender(
                "{{levelColor}}>> {{levelSymbol}} {{hh}}:{{mm}}:{{ss}}.{{SSS}} [{{threadId}}/{{coroutineName}}][{{name}}] {{message}}{{r}}"
            )
            level = Level.DEBUG
        }
    }

    /**
     * Executes the given [block] with a temporary configuration.
     *
     * @param config The [ConfigSpec] to apply for the duration of the block.
     * @param block The block of code to execute.
     * @return The result of the [block].
     */
    inline fun <reified R> withTestConfig( // @formatter:off
        crossinline config: ConfigSpec,
        block: () -> R
    ): R { // @formatter:on
        val oldConfig = configSpec
        return try {
            configSpec = {
                oldConfig()
                config()
            }
            block()
        }
        finally {
            configSpec = oldConfig
        }
    }

    /**
     * Executes the given [block] within a [TestScope] and a temporary configuration.
     *
     * This function sets the [testScope] for the duration of the block and applies
     * the provided [config].
     *
     * @param config The [ConfigSpec] to apply for the duration of the block.
     * @param block The block of code to execute.
     * @return The result of the [block].
     */
    context(scope: TestScope) suspend inline fun <reified R> withTestScope( // @formatter:off
        crossinline config: ConfigSpec = {},
        block: suspend TestScope.() -> R
    ): R { // @formatter:on
        val oldConfig = configSpec
        return try {
            configSpec = {
                oldConfig()
                config()
            }
            testScope = scope
            block(scope)
        }
        finally {
            configSpec = oldConfig
            testScope = null
        }
    }

    /**
     * Sets up the test context for the given [ContextBuilder].
     *
     * This adds a default logger name "CUT" and the current [testScope] (if present)
     * to the context.
     */
    context(builder: ContextBuilder) fun setupTestContext() = with(builder) {
        value(Name("CUT"))
        testScope?.let(::TestScopeElement)?.let(::value)
    }

    /**
     * Creates a new [Logger] with the given name and context.
     *
     * If [testScope] is present, it returns an [AsyncTestLogger] to handle
     * asynchronous logging in tests (e.g., for Karma).
     *
     * @param name The name of the logger.
     * @param contextSpec The [ContextSpec] to configure the logger's context.
     * @return A new [Logger] instance.
     */
    override fun createLogger(name: String?, contextSpec: ContextSpec): Logger {
        if (testScope != null) { // If we in a test scope, use async test logger for console workaround
            return AsyncTestLogger(Config(configSpec), Context {
                this@TestBackend.contextSpec(this)
                setupTestContext()
                contextSpec()
                name?.let(::Name)?.let(::value)
            })
        }
        return DefaultBackend.createLogger(name) {
            setupTestContext()
            contextSpec()
        }
    }
}