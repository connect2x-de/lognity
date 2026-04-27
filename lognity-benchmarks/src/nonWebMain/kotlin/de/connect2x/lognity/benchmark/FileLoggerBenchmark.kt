package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.fileAppender
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@State(Scope.Benchmark)
open class FileLoggerBenchmark : AbstractLoggerBenchmark<Logger>() {
    companion object {
        private const val PATH: String = "file_logger_benchmark.log"
    }

    init {
        Backend.set(DefaultBackend)
        Backend.configSpec = {
            fileAppender(
                path = PATH,
                pattern = "[{{level}}] {{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}"
            )
        }
    }

    override val logger: Logger = Logger("FileLoggerBenchmark")

    @TearDown
    fun tearDown() {
        SystemFileSystem.delete(Path(PATH), mustExist = false)
    }
}