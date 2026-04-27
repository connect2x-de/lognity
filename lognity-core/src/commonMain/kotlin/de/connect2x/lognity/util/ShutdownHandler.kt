package de.connect2x.lognity.util

internal expect object ShutdownHandler {
    fun register(block: () -> Unit, priority: Int = 0)
    fun invokeAll()
}