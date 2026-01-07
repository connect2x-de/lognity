package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import de.connect2x.lognity.config.util.isNode
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise

@PublishedApi
@OptIn(ExperimentalWasmJsInterop::class)
@JsName("fetch")
internal external fun fetch(input: String): Promise<Response>

@OptIn(ExperimentalWasmJsInterop::class)
actual fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    formatters: Map<String, Formatter>,
    block: () -> Unit
) {  // @formatter:on
    if (isNode) {
        setDefaultConfig(Path(path), formatters)
        block()
        return
    }
    fetch(path).then<JsAny?> { response ->
        response.text().then<JsAny?> { text ->
            val buffer = Buffer()
            buffer.writeString(text.toString())
            setDefaultConfig(buffer, formatters)
            null
        }.finally(block)
    }
}