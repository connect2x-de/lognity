package net.folivo.lognity.config

import kotlinx.browser.window
import kotlinx.io.Buffer
import kotlinx.io.writeString
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    window.fetch("./lognity.json").then { response ->
        response.text().then { text ->
            val buffer = Buffer()
            buffer.writeString(text)
            loadDefaultConfig(buffer, formatters)
        }
    }
}