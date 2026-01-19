package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun Backend.setDefaultConfig(source: Source) {
    val config = SerializableConfig.load(source)
    configSpec = { config.applyConfig() }
}

fun Backend.setDefaultConfig(path: Path) {
    SystemFileSystem.source(path).use { source ->
        setDefaultConfig(source.buffered())
    }
}

expect suspend inline fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    crossinline block: suspend () -> Unit
) // @formatter:on