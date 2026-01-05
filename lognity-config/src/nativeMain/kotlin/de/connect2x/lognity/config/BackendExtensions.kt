package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    SystemFileSystem.source(Path("lognity.json")).use { source ->
        loadDefaultConfig(source.buffered(), formatters)
    }
}