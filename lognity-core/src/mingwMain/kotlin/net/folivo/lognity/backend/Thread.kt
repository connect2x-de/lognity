package net.folivo.lognity.backend

import platform.windows.GetCurrentThreadId

// TODO: Implement a combination of GetThreadDescription & legacy debugger names
internal actual fun getThreadName(): String = getNativeThreadId().toString()

internal actual fun getNativeThreadId(): ULong = GetCurrentThreadId().toULong()