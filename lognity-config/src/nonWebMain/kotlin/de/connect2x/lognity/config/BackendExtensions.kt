@file:JvmName("BackendExtensionsJvm")

package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import kotlinx.io.files.Path
import kotlin.jvm.JvmName

actual suspend fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    block: suspend () -> Unit
) {  // @formatter:on
    setDefaultConfig(Path(path))
    block()
}