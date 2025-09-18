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

package net.folivo.lognity.java

import net.folivo.lognity.api.Logger
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger as JavaLogger

class LognityJavaLogger(
    private val delegate: Logger
) : JavaLogger(delegate.name, null) {
    override fun getLevel(): Level = delegate.level.asJavaLevel()

    override fun setLevel(newLevel: Level) {
        delegate.level = newLevel.asLognityLevel()
    }

    override fun log(record: LogRecord) {
        delegate.log(record.level.asLognityLevel()) { record.message }
    }
}

fun Logger.asJavaLogger(): LognityJavaLogger = LognityJavaLogger(this)