@file:OptIn(ExperimentalWasmJsInterop::class)

package de.connect2x.lognity.util

import web.window.window
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.unsafeCast

internal external interface ProcessEnv : JsAny {
    val LOGNITY_DEFAULT_LEVEL: String?
}

internal external interface Process : JsAny {
    val env: ProcessEnv
    fun on(signalName: String, callback: () -> Unit)
}

internal external interface ExtendedMediaQuery : JsAny {
    val matches: Boolean
}

internal external interface ExtendedWindow : JsAny {
    val webdriver: Boolean?
    fun matchMedia(query: String): ExtendedMediaQuery?
}

private fun checkIsNode(): Boolean = js("""typeof process !== 'undefined' && process.release.name === 'node'""")
private fun checkIsKarma(): Boolean = js("""window !== 'undefined' && window.__karma__ !== 'undefined'""")
private fun checkIsMocha(): Boolean = js("""typeof mocha !== 'undefined'""")

internal fun getProcess(): Process = js("""process""")
private fun getUserAgent(): String = js("""window.navigator.userAgent""")

internal val isNode: Boolean = checkIsNode()
internal val isChrome: Boolean = !isNode && "chrome" in getUserAgent().lowercase()
internal val isKarma: Boolean = !isNode && checkIsKarma()
internal val isMocha: Boolean = checkIsMocha()
internal val isTestRunner: Boolean get() = isKarma || isMocha

internal val isDarkColorScheme: Boolean by lazy {
    if (isNode || isTestRunner) return@lazy true
    val extWindow = window.unsafeCast<ExtendedWindow>()
    extWindow.webdriver == true || extWindow.matchMedia("(prefers-color-scheme: dark)")?.matches == true
}
