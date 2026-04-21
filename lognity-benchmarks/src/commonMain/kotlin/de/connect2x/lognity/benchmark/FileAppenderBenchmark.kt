package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.format.SimpleFormatter
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@State(Scope.Benchmark)
open class FileAppenderBenchmark : AbstractAppenderBenchmark<FileAppender>() {
    private val path: Path = Path("benchmark.txt")

    override fun create(): FileAppender = FileAppender(
        pattern = "[{{level}}] {{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}",
        filter = Filter.always,
        formatter = SimpleFormatter.default,
        path = path
    )

    @TearDown
    fun tearDown() {
        SystemFileSystem.delete(path, mustExist = false)
    }
}