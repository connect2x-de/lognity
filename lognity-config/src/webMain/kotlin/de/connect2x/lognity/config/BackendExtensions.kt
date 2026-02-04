package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.config.util.isNode
import js.promise.Promise
import js.promise.await
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import web.http.Response
import web.http.text
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsName

@OptIn(ExperimentalWasmJsInterop::class)
@JsName("fetch")
private external fun fetch(input: String): Promise<Response>

@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
actual suspend fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    block: suspend () -> Unit
) {  // @formatter:on
    if (isNode) {
        setDefaultConfig(Path(path))
        block()
        return
    }
    val response = fetch(path).await()
    val buffer = Buffer()
    buffer.writeString(response.text())
    setDefaultConfig(buffer)
    block()
}