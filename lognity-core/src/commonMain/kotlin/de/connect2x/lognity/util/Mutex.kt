@file:JvmName("Mutex$")

package de.connect2x.lognity.util

import kotlin.jvm.JvmName

internal interface Mutex {
    fun lock()
    suspend fun lockSuspend()
    fun unlock()
}

internal expect fun Mutex(): Mutex

internal inline fun <R> Mutex.withLock(action: () -> R): R {
    return try {
        lock()
        action()
    } finally {
        unlock()
    }
}

internal suspend inline fun <R> Mutex.withLockSuspend(action: () -> R): R {
    return try {
        lockSuspend()
        action()
    } finally {
        unlock()
    }
}
