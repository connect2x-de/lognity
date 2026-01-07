package de.connect2x.lognity.example

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.config.CoreConfigExtension
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.loadDefaultConfig
import kotlinx.io.files.Path

internal actual fun configureLognity() {
    // Register core config extension for builtin appenders and conditions before loading any configs
    SerializableConfig uses CoreConfigExtension
    // Load the JSON based config using the lognity-config module
    Backend.loadDefaultConfig(Path("example_config.json"))
}