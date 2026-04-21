package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.appender.RollingFileAppender
import de.connect2x.lognity.format.SimpleFormatter
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@State(Scope.Benchmark)
open class RollingFileAppenderBenchmark : AbstractAppenderBenchmark<RollingFileAppender>() {
    private val directory: Path = Path("rolling_file_benchmark")

    init {
        SystemFileSystem.createDirectories(directory)
    }

    override val appender: RollingFileAppender = RollingFileAppender(
        pattern = "[{{level}}] {{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}",
        filter = Filter.always,
        formatter = SimpleFormatter.default,
        basePath = Path(directory, "benchmark.txt"),
        useTimestamps = false,
        deleteExisting = true,
        latestSuffix = ".latest"
    )

    @TearDown
    fun tearDown() {
        for (entry in SystemFileSystem.list(directory)) {
            SystemFileSystem.delete(entry, mustExist = false)
        }
        SystemFileSystem.delete(directory, mustExist = false)
    }
}