package de.connect2x.lognity.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
open class PrintlnBenchmark {
    @Benchmark
    fun run() {
        println("001100 010010 011110 100001 101101 110011")
    }
}