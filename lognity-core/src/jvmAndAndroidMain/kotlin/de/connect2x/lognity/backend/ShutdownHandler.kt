package de.connect2x.lognity.backend

import java.util.concurrent.ConcurrentLinkedQueue

internal actual object ShutdownHandler {
    private val hooks: ConcurrentLinkedQueue<Pair<() -> Unit, Int>> = ConcurrentLinkedQueue()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            for ((hook, _) in hooks.sortedBy(Pair<() -> Unit, Int>::second)) hook()
        })
    }

    actual fun register(block: () -> Unit, priority: Int) {
        hooks += block to priority
    }
}