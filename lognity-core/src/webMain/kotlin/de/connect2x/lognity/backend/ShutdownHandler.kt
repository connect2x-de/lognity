package de.connect2x.lognity.backend

import kotlinx.browser.window

internal actual object ShutdownHandler {
    private val hooks: ArrayList<() -> Unit> = ArrayList()

    init {
        window.onunload = {
            for (hook in hooks) hook()
        }
    }

    actual fun register(block: () -> Unit) {
        hooks += block
    }
}