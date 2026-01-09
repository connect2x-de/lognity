@file:OptIn(ExperimentalWasmJsInterop::class)

package de.connect2x.lognity.util

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

internal external interface Process {
    fun on(signalName: String, callback: () -> Unit)
}

private fun checkIsNode(): Boolean = js("""typeof process !== "undefined" && process.release.name === "node"""")
internal fun getProcess(): Process = js("""process""")

internal val isNode: Boolean = checkIsNode()