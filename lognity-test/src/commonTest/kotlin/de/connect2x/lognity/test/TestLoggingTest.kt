package de.connect2x.lognity.test

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestLoggingTest {
    @BeforeTest
    fun setup() {
        Backend.setOnce(TestBackend)
    }

    @Test
    fun `Blocking test`() {
        val logger = Logger()
        for (i in 0..<10) {
            logger.info { "HELLO, WORLD!" }
        }
    }

    @Test
    fun `Suspending test`() = runTest {
        TestBackend.withTestScope {
            val logger = Logger()
            for (i in 0..<10) {
                logger.info { "HELLO, WORLD!" }
            }
        }
    }
}