package net.folivo.lognity.backend

import kotlinx.coroutines.sync.Mutex

internal actual fun getThreadName(): String = "main"

internal actual fun getNativeThreadId(): ULong = 0UL

internal actual inline fun <reified R> Mutex.withBlockingLock(crossinline action: () -> R): R {
    return action()
}