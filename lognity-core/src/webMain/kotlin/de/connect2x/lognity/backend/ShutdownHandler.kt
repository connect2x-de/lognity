package de.connect2x.lognity.backend

import de.connect2x.lognity.util.getProcess
import de.connect2x.lognity.util.isNode
import web.events.EventHandler
import web.window.window

internal actual object ShutdownHandler {
    private val hooks: ArrayList<Pair<() -> Unit, Int>> = ArrayList()

    init {
        registerHook()
    }

    private fun registerHook() {
        fun runCallbacks() {
            for ((hook, _) in hooks.sortedBy(Pair<() -> Unit, Int>::second)) hook()
        }
        if (isNode) { // When we are running under Node, we need to insert signal handlers
            val process = getProcess()
            process.on("SIGINT", ::runCallbacks)
            process.on("SIGTERM", ::runCallbacks)
            return
        }
        // Otherwise use the browser API
        window.onbeforeunload = EventHandler { runCallbacks() }
    }

    actual fun register(block: () -> Unit, priority: Int) {
        hooks += block to priority
    }
}