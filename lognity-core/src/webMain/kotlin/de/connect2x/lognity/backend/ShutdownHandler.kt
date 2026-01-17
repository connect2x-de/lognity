package de.connect2x.lognity.backend

import de.connect2x.lognity.util.getProcess
import de.connect2x.lognity.util.isNode
import web.events.EventHandler
import web.window.window

internal actual object ShutdownHandler {
    private val hooks: ArrayList<Pair<() -> Unit, Int>> = ArrayList()

    init {
        registerAutomaticShutdown()
    }

    private fun registerAutomaticShutdown() {
        if (isNode) { // When we are running under Node, we need to insert signal handlers
            val process = getProcess()
            process.on("SIGINT", ::shutdown)
            process.on("SIGTERM", ::shutdown)
            return
        }
        // Otherwise use the browser API
        window.onbeforeunload = EventHandler { shutdown() }
    }

    actual fun register(block: () -> Unit, priority: Int) {
        hooks += block to priority
    }

    private fun shutdown() {
        for ((hook, _) in hooks.sortedBy(Pair<() -> Unit, Int>::second)) hook()
    }
}