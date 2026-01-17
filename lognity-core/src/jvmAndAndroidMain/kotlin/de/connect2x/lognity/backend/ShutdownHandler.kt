package de.connect2x.lognity.backend

import java.util.concurrent.ConcurrentLinkedQueue

internal actual object ShutdownHandler {
    private val hooks: ConcurrentLinkedQueue<Pair<() -> Unit, Int>> = ConcurrentLinkedQueue()

    init {
        Runtime.getRuntime().addShutdownHook(Thread(::shutdown))
    }

    actual fun register(block: () -> Unit, priority: Int) {
        hooks += block to priority
    }

    private fun shutdown() {
        for ((hook, _) in hooks.sortedBy(Pair<() -> Unit, Int>::second)) hook()
    }
}