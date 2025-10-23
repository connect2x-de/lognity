package net.folivo.lognity.backend

internal expect object ShutdownHandler {
    fun register(block: () -> Unit)
}