@file:JvmName("BackendExtensionsJvm")

package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import kotlinx.io.asSource
import kotlinx.io.buffered

actual fun Backend.loadDefaultConfig(formatters: Map<String, Formatter>) {
    requireNotNull(this::class.java.getResourceAsStream("/lognity.json")).use { stream ->
        loadDefaultConfig(stream.asSource().buffered(), formatters)
    }
}