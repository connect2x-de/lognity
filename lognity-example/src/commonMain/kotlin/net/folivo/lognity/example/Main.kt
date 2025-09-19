/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.example

import kotlinx.io.files.Path
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.backend.Backend
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
    Backend.current.shutdown() // Shutdown backend, ensure resources are closed
}