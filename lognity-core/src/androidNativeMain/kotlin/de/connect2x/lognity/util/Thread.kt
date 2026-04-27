package de.connect2x.lognity.util

import kotlinx.cinterop.UnsafeNumber
import platform.posix.pthread_gettid_np
import platform.posix.pthread_self

// TODO: Try to find a better solution for this if possible
internal actual fun getThreadName(): String = getThreadId().toString()

@OptIn(UnsafeNumber::class)
internal actual fun getThreadId(): ULong = pthread_gettid_np(pthread_self()).toULong()