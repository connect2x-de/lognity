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

package net.folivo.lognity.backend

import kotlinx.coroutines.sync.Mutex

internal actual fun getThreadName(): String = "main"

internal actual fun getThreadId(): ULong = 0UL

internal actual inline fun <reified R> Mutex.withBlockingLock(crossinline action: () -> R): R {
    return action()
}