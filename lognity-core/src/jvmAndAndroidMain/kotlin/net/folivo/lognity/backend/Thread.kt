package net.folivo.lognity.backend

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal actual fun getThreadName(): String = Thread.currentThread().name

@Suppress("DEPRECATION") // Because we share this between JVM and Android
internal actual fun getThreadId(): ULong = Thread.currentThread().id.toULong()

internal actual inline fun <reified R> Mutex.withBlockingLock(crossinline action: () -> R): R {
    return runBlocking {
        withLock { action() }
    }
}