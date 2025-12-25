package de.connect2x.lognity.example

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.backend.Platform
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
import de.connect2x.lognity.config.CoreConfig
import de.connect2x.lognity.config.loadDefaultConfig
import kotlinx.io.files.Path

private fun Logger.printTestMessages() {
    trace { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    debug { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    info { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    warn { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    error { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    fatal { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
}

fun main() {
    CoreConfig.install() // Install all config extensions for the core module
    Backend.set(DefaultBackend)
    // Load the JSON based config using the lognity-config module
    Backend.loadDefaultConfig(Path("example_config.json"))
    // Overwrite the default context created for every new Logger instance
    Backend.contextSpec = {
        value(Logger.Name("My Default Logger"))
        onlyOn(Platform.LINUX) {
            value(Logger.Name("My Penguin"))
        }
    }
    // Explicitly named logger
    Logger("My Logger").printTestMessages()
    // Implicitly named logger (default defined in example_config.json)
    Logger().printTestMessages()
}