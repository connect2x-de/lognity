package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.systemConsoleAppender
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
open class ConsoleLoggerBenchmark : AbstractLoggerBenchmark<Logger>() {
    init {
        Backend.set(DefaultBackend)
        Backend.configSpec = {
            systemConsoleAppender("[{{level}}] {{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}")
        }
    }

    override val logger: Logger = Logger("ConsoleLoggerBenchmark")
}