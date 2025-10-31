package net.folivo.lognity.config

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    SystemFileSystem.source(Path("lognity.json")).use { source ->
        loadDefaultConfig(source.buffered(), formatters)
    }
}