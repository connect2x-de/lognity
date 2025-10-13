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
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.config.ConfigBuilder
import net.folivo.lognity.api.format.Formatter

object ConfigLoader {
    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        prettyPrintIndent = "\t"
        allowComments = true
        allowTrailingComma = true
        serializersModule = SerializersModule {
            polymorphic(SerializableAppender::class) {
                subclass(SerializableAppender.Console::class)
                subclass(SerializableAppender.File::class)
            }
            polymorphic(SerializableFilter.Condition::class) {
                subclass(SerializableFilter.AlwaysCondition::class)
                subclass(SerializableFilter.LevelCondition::class)
                subclass(SerializableFilter.MarkerCondition::class)
                subclass(SerializableFilter.MessageCondition::class)
            }
        }
    }

    fun load( // @formatter:off
        source: Source,
        formatters: Map<String, Formatter> = mapOf("default" to Backend.current.defaultFormatter)
    ): ConfigBuilder.() -> Unit {
        val config = json.decodeFromString<SerializableConfig>(source.readString())
        check(config.version == SerializableConfig.VERSION) {
            "Incompatible lognity config version ${config.version}, expected at least ${SerializableConfig.VERSION}"
        }
        return {
            level = config.level
            isEnabled = config.enabled
            for(appender in config.appenders) when(appender) {
                is SerializableAppender.Console -> {
                    val formatter = formatters[appender.formatter]!!
                    platformConsoleAppender(appender.pattern, formatter, appender.filter)
                }
                is SerializableAppender.File -> {
                    val formatter = formatters[appender.formatter]!!
                    fileAppender(appender.pattern, formatter, appender.filter, appender.path)
                }
            }
        }
    }
}