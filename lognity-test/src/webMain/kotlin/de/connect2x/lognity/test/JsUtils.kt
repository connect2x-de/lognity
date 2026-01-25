package de.connect2x.lognity.test

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
private fun checkIsKarma(): Boolean = js("""typeof window !== 'undefined' && typeof window.__karma__ !== 'undefined'""")

internal val isKarma: Boolean = checkIsKarma()