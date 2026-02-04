package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Sets the default configuration for the [Backend] from the given [Source].
 *
 * @param source the source containing the JSON configuration.
 */
fun Backend.setDefaultConfig(source: Source) {
    val config = SerializableConfig.load(source)
    configSpec = { config.applyConfig() }
}

/**
 * Sets the default configuration for the [Backend] from the given [Path].
 *
 * @param path the path to the JSON configuration file.
 */
fun Backend.setDefaultConfig(path: Path) {
    SystemFileSystem.source(path).use { source ->
        setDefaultConfig(source.buffered())
    }
}

/**
 * Executes the given [block] with the default configuration loaded from the given [path].
 *
 * @param path the path to the JSON configuration file.
 * @param block the block to execute.
 */
expect suspend fun Backend.withDefaultConfig( // @formatter:off
    path: String,
    block: suspend () -> Unit
) // @formatter:on