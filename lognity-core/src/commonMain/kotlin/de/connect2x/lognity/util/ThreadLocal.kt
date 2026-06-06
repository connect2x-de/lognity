package de.connect2x.lognity.util

import co.touchlab.stately.concurrency.ThreadLocalRef

internal class ThreadLocal<T>(
    private val initializer: () -> T
) {
    private val ref: ThreadLocalRef<T> = ThreadLocalRef()

    fun get(): T {
        val oldValue = ref.get()
        if(oldValue != null) return oldValue
        val newValue = initializer()
        ref.set(newValue)
        return newValue
    }
}
