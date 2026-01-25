package de.connect2x.lognity.util

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

internal external interface Process {
    fun on(signalName: String, callback: () -> Unit)
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun checkIsNode(): Boolean = js("""typeof process !== 'undefined' && process.release.name === 'node'""")

@OptIn(ExperimentalWasmJsInterop::class)
internal fun getProcess(): Process = js("""process""")

@OptIn(ExperimentalWasmJsInterop::class)
private fun getUserAgent(): String = js("""window.navigator.userAgent""")

internal val isNode: Boolean = checkIsNode()
internal val isChrome: Boolean = !isNode && "chrome" in getUserAgent().lowercase()