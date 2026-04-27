@file:OptIn(ExperimentalForeignApi::class)

package de.connect2x.lognity.util

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
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

internal actual fun getThreadName(): String = memScoped {
    val name = allocArray<ByteVar>(MAX_NAME_LENGTH)
    pthread_getname_np(pthread_self(), name, MAX_NAME_LENGTH.convert())
    // On Apple platforms, pthread_getname_np may return an empty string if the program
    // has set no thread name explicitly. In that case, we default to the thread ID.
    name.toKStringFromUtf8().ifEmpty { getThreadId().toString() }
}

internal actual fun getThreadId(): ULong = memScoped {
    val id = alloc<uint64_tVar>()
    pthread_threadid_np(pthread_self(), id.ptr)
    id.value
}