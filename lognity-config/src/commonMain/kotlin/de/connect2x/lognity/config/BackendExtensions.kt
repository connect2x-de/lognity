package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.format.Formatter
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Loads a Lognity configuration from the given Source and applies it to this Backend.
 *
 * The configuration is expected to be a JSON document following the Lognity config schema.
 *
 * - The provided formatters map resolves formatter names referenced by the configuration
 *   (defaults to a single entry named "default").
 * - After loading, the backend's configSpec is updated to apply the parsed configuration
 *   when the backend is (re)initialized.
 *
 * @param source a readable source containing the JSON configuration
 * @param formatters a map of formatter identifiers to Formatter implementations used by the config
 */
fun Backend.setDefaultConfig( // @formatter:off
    source: Source,
    formatters: Map<String, Formatter> = mapOf("default" to Formatter.default)
) { // @formatter:on
    val config = SerializableConfig.load(source)
    configSpec = { config.applyConfig(formatters) }
}

/**
 * Loads a Lognity configuration from a file at the given path and applies it to this Backend.
 *
 * The file must contain a JSON configuration matching the Lognity config schema.
 *
 * @param path the path to the configuration file (e.g., "lognity.json")
 * @param formatters a map of formatter identifiers used by the configuration
 */
fun Backend.setDefaultConfig( // @formatter:off
    path: Path,
    formatters: Map<String, Formatter> = mapOf("default" to Formatter.default)
) { // @formatter:on
    SystemFileSystem.source(path).use { source ->
        setDefaultConfig(source.buffered(), formatters)
    }
}

expect fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    formatters: Map<String, Formatter> = mapOf("default" to Formatter.default),
    block: () -> Unit
) // @formatter:on