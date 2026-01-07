@file:JvmName("BackendExtensionsJvm")

package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import kotlinx.io.files.Path
import kotlin.jvm.JvmName

actual fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    formatters: Map<String, Formatter>,
    block: () -> Unit
) {  // @formatter:on
    setDefaultConfig(Path(path), formatters)
    block()
}