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
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import platform.linux.PR_GET_NAME
import platform.linux.SYS_gettid
import platform.linux.prctl
import platform.posix.syscall

private const val MAX_NAME_LENGTH: Int = 16

@OptIn(ExperimentalForeignApi::class)
internal actual fun getThreadName(): String = memScoped {
    val name = allocArray<ByteVar>(MAX_NAME_LENGTH)
    prctl(PR_GET_NAME, name, 0, 0, 0)
    name.toKStringFromUtf8()
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getThreadId(): ULong = syscall(SYS_gettid.convert()).toULong()