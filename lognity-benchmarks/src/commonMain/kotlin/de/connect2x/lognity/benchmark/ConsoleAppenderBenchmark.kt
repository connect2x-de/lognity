package de.connect2x.lognity.benchmark

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.appender.ConsoleAppender
import de.connect2x.lognity.format.SimpleFormatter
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
open class ConsoleAppenderBenchmark : AbstractAppenderBenchmark<ConsoleAppender>() {
    override fun create(): ConsoleAppender = ConsoleAppender(
        pattern = "[{{level}}] {{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}",
        filter = Filter.always,
        formatter = SimpleFormatter.default
    )
}