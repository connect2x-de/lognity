@file:JvmName("BackendExtensionsJvm")

package net.folivo.lognity.config

import kotlinx.io.asSource
import kotlinx.io.buffered
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    requireNotNull(this::class.java.getResourceAsStream("/lognity.json")).use { stream ->
        loadDefaultConfig(stream.asSource().buffered(), formatters)
    }
}