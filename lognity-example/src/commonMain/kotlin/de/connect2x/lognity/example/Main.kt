package de.connect2x.lognity.example

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.CoreConfigExtension
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.withDefaultConfig

private fun Logger.printTestMessages() {
    trace { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    debug { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    info { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    warn { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    error { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    fatal { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
}

suspend fun main() {
    // Set the backend implementation
    Backend.set(DefaultBackend)
    // Configure Lognity based on platform
    // Register core config extension for builtin appenders and conditions before loading any configs
    SerializableConfig uses CoreConfigExtension
    // Load the JSON based config using the lognity-config module
    Backend.withDefaultConfig("example_config.json") {
        // Overwrite the default context created for every new Logger instance
        Backend.contextSpec = {
            value(Logger.Name("My Default Logger"))
        }
        // Explicitly named logger
        Logger("My Logger").printTestMessages()
        // Implicitly named logger (default defined in example_config.json)
        Logger().printTestMessages()
    }
}