package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.marker.Marker
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExceptionLoggingTest {
    private class CapturedLog(
        val level: Level, val marker: Marker?, val message: String
    )

    private class TestLogger : Logger {
        override val context: Context = Context {}
        override val config: Config = Config()
        override var level: Level = Level.TRACE
        override var isEnabled: Boolean = true

        val logs = mutableListOf<CapturedLog>()

        override fun log(level: Level, message: MessageProvider) {
            logs.add(CapturedLog(level, null, message(this).toString()))
        }

        override fun log(marker: Marker?, level: Level, message: MessageProvider) {
            logs.add(CapturedLog(level, marker, message(this).toString()))
        }
    }

    private val logger: TestLogger = TestLogger()

    private object TestMarker : Marker {
        override val key: String = "test-key"
        override val name: String = "test-name"
        override var isEnabled: Boolean = true
    }

    @BeforeTest
    fun setUp() {
        logger.logs.clear()
    }

    @Test
    fun `trace with throwable and marker`() {
        val exception = RuntimeException("Test exception")
        logger.trace(exception, TestMarker) { "Trace message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.TRACE, log.level)
        assertEquals(TestMarker, log.marker)
        assertTrue(log.message.startsWith("Trace message: "))
        assertTrue(log.message.contains("RuntimeException"))
        assertTrue(log.message.contains("Test exception"))
        assertTrue(log.message.contains(ExceptionLoggingTest::class.simpleName!!))
    }

    @Test
    fun `trace with null throwable`() {
        logger.trace(throwable = null) { "Trace message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.TRACE, log.level)
        assertEquals("Trace message: Stacktrace unavailable", log.message)
    }

    @Test
    fun `debug with throwable and marker`() {
        val exception = RuntimeException("Test exception")
        logger.debug(exception, TestMarker) { "Debug message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.DEBUG, log.level)
        assertEquals(TestMarker, log.marker)
        assertTrue(log.message.startsWith("Debug message: "))
        assertTrue(log.message.contains("RuntimeException"))
        assertTrue(log.message.contains("Test exception"))
    }

    @Test
    fun `debug with null throwable`() {
        logger.debug(throwable = null) { "Debug message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.DEBUG, log.level)
        assertEquals("Debug message: Stacktrace unavailable", log.message)
    }

    @Test
    fun `info with throwable and marker`() {
        val exception = RuntimeException("Test exception")
        logger.info(exception, TestMarker) { "Info message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.INFO, log.level)
        assertEquals(TestMarker, log.marker)
        assertTrue(log.message.startsWith("Info message: "))
        assertTrue(log.message.contains("RuntimeException"))
        assertTrue(log.message.contains("Test exception"))
    }

    @Test
    fun `info with null throwable`() {
        logger.info(throwable = null) { "Info message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.INFO, log.level)
        assertEquals("Info message: Stacktrace unavailable", log.message)
    }

    @Test
    fun `warn with throwable and marker`() {
        val exception = RuntimeException("Test exception")
        logger.warn(exception, TestMarker) { "Warn message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.WARN, log.level)
        assertEquals(TestMarker, log.marker)
        assertTrue(log.message.startsWith("Warn message: "))
        assertTrue(log.message.contains("RuntimeException"))
        assertTrue(log.message.contains("Test exception"))
    }

    @Test
    fun `warn with null throwable`() {
        logger.warn(throwable = null) { "Warn message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.WARN, log.level)
        assertEquals("Warn message: Stacktrace unavailable", log.message)
    }

    @Test
    fun `error with throwable and marker`() {
        val exception = RuntimeException("Test exception")
        logger.error(exception, TestMarker) { "Error message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.ERROR, log.level)
        assertEquals(TestMarker, log.marker)
        assertTrue(log.message.startsWith("Error message: "))
        assertTrue(log.message.contains("RuntimeException"))
        assertTrue(log.message.contains("Test exception"))
    }

    @Test
    fun `error with null throwable`() {
        logger.error(throwable = null) { "Error message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.ERROR, log.level)
        assertEquals("Error message: Stacktrace unavailable", log.message)
    }

    @Test
    fun `fatal with throwable and marker`() {
        val exception = RuntimeException("Test exception")
        logger.fatal(exception, TestMarker) { "Fatal message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.FATAL, log.level)
        assertEquals(TestMarker, log.marker)
        assertTrue(log.message.startsWith("Fatal message: "))
        assertTrue(log.message.contains("RuntimeException"))
        assertTrue(log.message.contains("Test exception"))
    }

    @Test
    fun `fatal with null throwable`() {
        logger.fatal(throwable = null) { "Fatal message" }

        assertEquals(1, logger.logs.size)
        val log = logger.logs[0]
        assertEquals(Level.FATAL, log.level)
        assertEquals("Fatal message: Stacktrace unavailable", log.message)
    }
}
