package net.folivo.lognity.backend

import kotlinx.coroutines.sync.Mutex

internal expect fun getThreadName(): String

internal expect fun getNativeThreadId(): ULong

internal expect inline fun <reified R> Mutex.withBlockingLock(crossinline action: () -> R): R