package de.connect2x.lognity.config.util

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
private fun checkIsNode(): Boolean = js("""typeof process !== "undefined" && process.release.name === "node"""")

@PublishedApi
internal val isNode: Boolean = checkIsNode()