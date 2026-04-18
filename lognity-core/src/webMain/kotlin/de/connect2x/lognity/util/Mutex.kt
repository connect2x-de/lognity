package de.connect2x.lognity.util

private object MutexImpl : Mutex {
    override fun lock() = Unit
    override suspend fun lockSuspend() = Unit
    override fun unlock() = Unit
}

internal actual fun Mutex(): Mutex = MutexImpl