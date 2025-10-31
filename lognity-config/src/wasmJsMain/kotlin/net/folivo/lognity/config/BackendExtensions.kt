package net.folivo.lognity.config

import kotlinx.io.Buffer
import kotlinx.io.writeString
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter
import kotlin.js.Promise

external interface Response : JsAny {
    fun text(): Promise<JsString?>
}

@Suppress("UNUSED_PARAMETER")
private fun fetch(url: String): Promise<Response?> = js("window.fetch(url)")

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    fetch("./lognity.json").then<JsAny?> { response ->
        response?.text()?.then<JsAny?> { text ->
            val buffer = Buffer()
            buffer.writeString(requireNotNull(text?.toString()))
            loadDefaultConfig(buffer, formatters)
            null
        }
        null
    }
}