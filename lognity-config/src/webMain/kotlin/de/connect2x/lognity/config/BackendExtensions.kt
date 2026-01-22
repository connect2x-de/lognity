package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.config.util.isNode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import web.http.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
@PublishedApi
@JsName("fetch")
internal external fun fetch(input: String): Promise<Response>

@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
actual suspend inline fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    crossinline block: suspend () -> Unit
) {  // @formatter:on
    if (isNode) {
        setDefaultConfig(Path(path))
        block()
        return
    }
    fetch(path).then<JsAny?> { response ->
        response.textAsync().then<JsAny?> { text ->
            val buffer = Buffer()
            buffer.writeString(text.toString())
            setDefaultConfig(buffer)
            null
        }.finally {
            GlobalScope.launch { block() }
        }
    }
}