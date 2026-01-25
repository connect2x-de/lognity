package de.connect2x.lognity.test

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.deriveSuspend
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestLoggingTest {
    private fun Logger.printTestMessages() {
        trace { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
        debug { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
        info { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
        warn { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
        error { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
        fatal { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    }

    @BeforeTest
    fun setup() {
        Backend.setOnce(TestBackend)
    }

    @Test
    fun `Blocking test`() {
        val logger = Logger()
        for (i in 0..<10) {
            logger.printTestMessages()
        }
    }

    @Test
    fun `Suspending test`() = runTest {
        TestBackend.withTestScope {
            launch(testScheduler + CoroutineName("Test Coroutine")) {
                val logger = Logger().deriveSuspend() // Capture name from current coroutine
                for (i in 0..<10) {
                    logger.printTestMessages()
                }
            }.join()
        }
    }
}