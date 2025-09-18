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

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import platform.posix.pthread_getname_np
import platform.posix.pthread_self
import platform.posix.pthread_threadid_np
import platform.posix.uint64_tVar

private const val MAX_NAME_LENGTH: Int = 256

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual fun getThreadName(): String = memScoped {
    val name = allocArray<ByteVar>(MAX_NAME_LENGTH)
    pthread_getname_np(pthread_self(), name, MAX_NAME_LENGTH.convert())
    name.toKStringFromUtf8()
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getThreadId(): ULong = memScoped {
    val id = alloc<uint64_tVar>()
    pthread_threadid_np(pthread_self(), id.ptr)
    id.value
}