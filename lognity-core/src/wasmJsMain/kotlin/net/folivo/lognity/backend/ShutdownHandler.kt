package net.folivo.lognity.backend

@Suppress("UNUSED_PARAMETER")
private fun registerUnloadHandler(block: () -> Unit) {
    js("window.addEventListener('unload', (_) => { block(); });")
}

internal actual object ShutdownHandler {
    private val hooks: ArrayList<() -> Unit> = ArrayList()

    init {
        registerUnloadHandler {
            for (hook in hooks) hook()
        }
    }

    actual fun register(block: () -> Unit) {
        hooks += block
    }
}