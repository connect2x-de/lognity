@file:OptIn(ExperimentalForeignApi::class)

package de.connect2x.lognity.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.GetCurrentThreadId

// TODO: Implement a combination of GetThreadDescription & legacy debugger names
internal actual fun getThreadName(): String = getThreadId().toString()

internal actual fun getThreadId(): ULong = GetCurrentThreadId().toULong()