package de.connect2x.lognity.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

@State(Scope.Benchmark)
open class FileWriteBenchmark {
    private val path: Path = Path("file_benchmark.txt")
    private val sink: Sink = SystemFileSystem.sink(path).buffered()

    @Benchmark
    fun run() {
        sink.writeString("001100 010010 011110 100001 101101 110011\n")
    }

    @TearDown
    fun tearDown() {
        sink.close()
        SystemFileSystem.delete(path, mustExist = false)
    }
}