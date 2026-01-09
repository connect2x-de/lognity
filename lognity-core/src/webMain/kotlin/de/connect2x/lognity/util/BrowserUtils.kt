package de.connect2x.lognity.util

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
private fun getUserAgent(): String = js("""window.navigator.userAgent""")

internal val isChrome: Boolean = !isNode && "chrome" in getUserAgent().lowercase()