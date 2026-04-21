package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.appender.Appender
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.NoopLogger
import kotlinx.benchmark.Benchmark

abstract class AbstractAppenderBenchmark<A : Appender> {
    protected abstract val appender: A

    protected val dummyLogger: Logger = object : Logger by NoopLogger {
        override var isEnabled: Boolean = true
        override var level: Level = Level.TRACE
    }

    @Benchmark
    fun run() {
        appender.append(dummyLogger, Level.INFO, "001100 010010 011110 100001 101101 110011", null)
    }
}