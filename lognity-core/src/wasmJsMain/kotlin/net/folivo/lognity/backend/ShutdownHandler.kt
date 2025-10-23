package net.folivo.lognity.backend

internal actual object ShutdownHandler {
    private val hooks: ArrayList<() -> Unit> = ArrayList()

    init {
        @Suppress("UNUSED") val hooks = this.hooks // Guarantee hooks being captured into shared JS scope
        js(
            """
            window.addEventListener('unload', (_) => {
                const n = hooks.size;
                for(let i = 0; i < n; i++) {
                    hooks.get(i)();
                }
            });
        """.trimIndent()
        )
    }

    actual fun register(block: () -> Unit) {
        hooks += block
    }
}