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

import net.folivo.lognity.api.Logger
import io.ktor.util.logging.Logger as KtorLogger

expect class LognityKtorLogger(delegate: Logger) : KtorLogger {
    override fun debug(message: String)
    override fun debug(message: String, cause: Throwable)
    override fun error(message: String)
    override fun error(message: String, cause: Throwable)
    override fun info(message: String)
    override fun info(message: String, cause: Throwable)
    override fun trace(message: String)
    override fun trace(message: String, cause: Throwable)
    override fun warn(message: String)
    override fun warn(message: String, cause: Throwable)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Logger.asKtorLogger(): KtorLogger = LognityKtorLogger(this)