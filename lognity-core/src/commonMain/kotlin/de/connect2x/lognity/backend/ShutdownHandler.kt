package de.connect2x.lognity.backend

internal expect object ShutdownHandler {
    fun register(block: () -> Unit, priority: Int = 0)
}