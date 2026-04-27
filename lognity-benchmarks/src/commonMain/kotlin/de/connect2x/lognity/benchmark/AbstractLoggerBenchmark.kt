package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.logger.Logger
import kotlinx.benchmark.Benchmark

abstract class AbstractLoggerBenchmark<L : Logger> {
    protected abstract val logger: L

    @Benchmark
    fun run() {
        logger.info { "001100 010010 011110 100001 101101 110011" }
    }
}