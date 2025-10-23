package net.folivo.lognity.backend

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal actual inline fun <reified R> Mutex.withBlockingLock(crossinline action: () -> R): R {
    return runBlocking {
        withLock { action() }
    }
}