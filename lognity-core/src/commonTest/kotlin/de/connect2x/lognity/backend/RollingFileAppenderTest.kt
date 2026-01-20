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
        Backend.set(DefaultBackend)
    }

    @Test
    fun `File rotates on specified size boundary`() {
        Backend.configSpec = {
            systemConsoleAppender("{{message}}")
            rollingFileAppender("rolling_file_test.log", "{{message}}", maxFileCount = 2, maxFileSize = 1024)
        }
        val logger = Logger()
        val line = "X".repeat(127)
        for (i in 0..<10) {
            logger.info { line }
        }
    }
}