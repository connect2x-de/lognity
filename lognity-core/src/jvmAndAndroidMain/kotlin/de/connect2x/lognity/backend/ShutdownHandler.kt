package de.connect2x.lognity.backend

import java.util.concurrent.ConcurrentLinkedQueue

internal actual object ShutdownHandler {
    private val hooks: ConcurrentLinkedQueue<() -> Unit> = ConcurrentLinkedQueue()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            for (hook in hooks) hook()
        })
    }

    actual fun register(block: () -> Unit) {
        hooks += block
    }
}