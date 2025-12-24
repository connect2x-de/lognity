package de.connect2x.lognity.example

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.backend.DefaultBackend
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
    Backend.set(DefaultBackend)
    Backend.loadDefaultConfig(Path("example_config.json"))
    // Explicitly named logger
    Logger("My Logger").printTestMessages()
    // Implicitly named logger (default defined in example_config.json)
    Logger().printTestMessages()
}