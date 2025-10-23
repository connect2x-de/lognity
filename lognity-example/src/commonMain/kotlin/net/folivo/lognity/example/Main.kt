package net.folivo.lognity.example

import kotlinx.io.files.Path
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.logger.Logger
import net.folivo.lognity.backend.DefaultBackend
import net.folivo.lognity.config.loadDefaultConfig

fun main() {
    Backend.current = DefaultBackend
    Backend.current.loadDefaultConfig(Path("example_config.json"))
    val logger = Logger("My Logger")
    logger.trace { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    logger.debug { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    logger.info { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    logger.warn { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    logger.error { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
    logger.fatal { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" }
}