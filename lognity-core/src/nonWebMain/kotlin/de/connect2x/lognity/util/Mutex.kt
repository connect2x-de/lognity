package de.connect2x.lognity.util

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex as KxMutex

private class MutexImpl : Mutex {
    private val delegate: KxMutex = KxMutex()

    override fun lock() = runBlocking {
        delegate.lock()
    }

    override suspend fun lockSuspend() {
        delegate.lock()
    }

    override fun unlock() {
        delegate.unlock()
    }
}

internal actual fun Mutex(): Mutex = MutexImpl()