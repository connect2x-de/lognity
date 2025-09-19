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

package net.folivo.lognity.config

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.format.SimpleFormatter

fun Backend.loadDefaultConfig( // @formatter:off
    source: Source,
    formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default)
) { // @formatter:on
    defaultConfigSpec = ConfigLoader.load(source, formatters)
}

fun Backend.loadDefaultConfig( // @formatter:off
    path: Path,
    formatters: Map<String, Formatter> = mapOf("default" to SimpleFormatter.default)
) { // @formatter:on
    SystemFileSystem.source(path).use { source ->
        loadDefaultConfig(source.buffered(), formatters)
    }
}