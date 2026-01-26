package de.connect2x.lognity.example

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.CoreConfigExtension
import de.connect2x.lognity.config.SerializableConfig
import de.connect2x.lognity.config.extension.ConfigExtension
import de.connect2x.lognity.config.withDefaultConfig
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val marker1: Marker = Marker("marker1")

private fun Logger.printTestMessages(marker: Marker? = null) {
    trace(marker) { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    debug(marker) { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    info(marker) { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    warn(marker) { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    error(marker) { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    fatal(marker) { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
}

suspend fun appMain() {
    // Set the backend implementation
    Backend.set(DefaultBackend)
    // Configure Lognity based on platform
    // Register core config extension for builtin appenders and conditions before loading any configs
    SerializableConfig uses CoreConfigExtension
    // Register custom providers to use in the config with {NAME} notation
    SerializableConfig uses ConfigExtension {
        registerProvider("DEFAULT_LOG_LEVEL") { Level.TRACE }
        registerProvider("LOG_DIRECTORY") { "logs" }
    }
    // Load the JSON based config using the lognity-config module
    Backend.withDefaultConfig("example_config.json") {
        // Overwrite the default context created for every new Logger instance
        Backend.contextSpec = {
            value(Logger.Name("My Default Logger"))
        }
        // Explicitly named logger
        val namedLogger = Logger("My Logger")
        namedLogger.printTestMessages()
        // Implicitly named logger (default defined in example_config.json)
        Logger().printTestMessages(marker1)

        val anotherLogger = Logger("My Filtered Logger")
        (0..<4).map {
            DefaultBackend.coroutineScope.launch {
                for (i in 0..<50) anotherLogger.printTestMessages()
            }
        }.joinAll()
    }
}