package net.folivo.lognity.config

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.format.SimpleFormatter

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
fun Backend.loadDefaultConfig( // @formatter:off
    source: Source,
    formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default)
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
fun Backend.loadDefaultConfig( // @formatter:off
    path: Path,
    formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default)
) { // @formatter:on
    SystemFileSystem.source(path).use { source ->
        loadDefaultConfig(source.buffered(), formatters)
    }
}

/**
 * Load the default configuration for Lognity using the platform's preferred
 * way of loading files.
 * - For JVM and Android JVM, `lognity.json` is loaded from the resources root
 * - For JS and WASM/JS, `lognity.json` is loaded from the web content root
 * - For native targets, `lognity.json` is loaded relative to the executable
 *
 * @param formatters a map of formatter identifiers to Formatter implementations used by the config
 */
expect fun Backend.loadDefaultConfig(formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default))