package de.connect2x.lognity.backend

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.config.rollingFileAppender
import de.connect2x.lognity.config.systemConsoleAppender
import kotlin.test.BeforeTest
import kotlin.test.Test

class RollingFileAppenderTest {
    @BeforeTest
    fun setup() {
        Backend.setOnce(DefaultBackend)
    }

    @Test
    fun `File rotates on specified size boundary`() {
        Backend.configSpec = {
            systemConsoleAppender("{{message}}")
            rollingFileAppender(
                basePath = "rolling_file_size_test.log",
                pattern = "{{message}}",
                maxFileCount = 2,
                maxFileSize = 1024,
                useTimestamps = false
            )
        }
        val logger = Logger()
        val line = "X".repeat(127)
        for (i in 0..<10) {
            logger.info { line }
        }
    }

    @Test
    fun `File rotates and wraps around after last segment`() {
        Backend.configSpec = {
            systemConsoleAppender("{{message}}")
            rollingFileAppender(
                basePath = "rolling_file_wrap_test.log",
                pattern = "{{message}}",
                maxFileCount = 2,
                maxFileSize = 300,
                useTimestamps = false
            )
        }
        val logger = Logger()
        val line = "X".repeat(127)
        for (i in 0..<10) {
            logger.info { line }
        }
    }
}