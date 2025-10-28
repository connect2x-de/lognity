package net.folivo.lognity.config

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.format.SimpleFormatter

fun Backend.loadDefaultConfig( // @formatter:off
    source: Source,
    formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default)
) { // @formatter:on
    val config = SerializableConfig.load(source)
    configSpec = { config.applyConfig(formatters) }
    contextSpec = { config.applyContext() }
}

fun Backend.loadDefaultConfig( // @formatter:off
    path: Path,
    formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default)
) { // @formatter:on
    SystemFileSystem.source(path).use { source ->
        loadDefaultConfig(source.buffered(), formatters)
    }
}