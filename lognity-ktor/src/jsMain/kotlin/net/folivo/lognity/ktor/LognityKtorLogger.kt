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

package net.folivo.lognity.ktor

import io.ktor.util.logging.LogLevel
import io.ktor.util.logging.Logger as KtorLogger
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.debug
import net.folivo.lognity.api.error
import net.folivo.lognity.api.info
import net.folivo.lognity.api.trace
import net.folivo.lognity.api.warn

actual class LognityKtorLogger actual constructor(
    private val delegate: Logger
) : KtorLogger {
    actual override fun debug(message: String) = delegate.debug { message }
    actual override fun debug(message: String, cause: Throwable) = delegate.debug(cause) { message }
    actual override fun error(message: String) = delegate.error { message }
    actual override fun error(message: String, cause: Throwable) = delegate.error(cause) { message }
    actual override fun info(message: String) = delegate.info { message }
    actual override fun info(message: String, cause: Throwable) = delegate.info(cause) { message }
    actual override fun trace(message: String) = delegate.trace { message }
    actual override fun trace(message: String, cause: Throwable) = delegate.trace(cause) { message }
    actual override fun warn(message: String) = delegate.warn { message }
    actual override fun warn(message: String, cause: Throwable) = delegate.warn(cause) { message }

    override val level: LogLevel
        get() = delegate.level.asKtorLevel()
}