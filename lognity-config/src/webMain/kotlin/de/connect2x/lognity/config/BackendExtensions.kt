@file:OptIn(ExperimentalWasmJsInterop::class)

package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import kotlinx.browser.window
import kotlinx.io.Buffer
import kotlinx.io.writeString
import org.w3c.fetch.RequestInit
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    window.fetch("./lognity.json", init = RequestInit()).then<JsAny?> { response ->
        response.text().then<JsAny?> { text ->
            val buffer = Buffer()
            buffer.writeString(text.toString())
            loadDefaultConfig(buffer, formatters)
            null
        }
        null
    }
}