package net.folivo.lognity.backend

import kotlinx.cinterop.UnsafeNumber
import platform.posix.pthread_gettid_np
import platform.posix.pthread_self

// TODO: Try to find a better solution for this if possible
internal actual fun getThreadName(): String = getNativeThreadId().toString()

@OptIn(UnsafeNumber::class)
internal actual fun getNativeThreadId(): ULong = pthread_gettid_np(pthread_self()).toULong()