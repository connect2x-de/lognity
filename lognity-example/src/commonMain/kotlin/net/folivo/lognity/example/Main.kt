package net.folivo.lognity.example

import kotlinx.io.files.Path
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.backend.DefaultBackend
import net.folivo.lognity.config.loadDefaultConfig

private fun Logger.printTestMessages() {
    trace { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    debug { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    info { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    warn { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    error { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    fatal { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
}

fun main() {
    Backend.current = DefaultBackend
    Backend.current.loadDefaultConfig(Path("example_config.json"))
    // Explicitly named logger
    Logger("My Logger").printTestMessages()
    // Implicitly named logger (default defined in example_config.json)
    Logger().printTestMessages()
}