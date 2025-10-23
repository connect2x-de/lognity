package net.folivo.lognity.util

import co.touchlab.stately.concurrency.ThreadLocalRef

internal class ThreadLocal<T>(
    private val initializer: () -> T
) {
    private val ref: ThreadLocalRef<T> = ThreadLocalRef()

    fun get(): T {
        var value = ref.get()
        if (value == null) {
            value = initializer()
            ref.set(value)
        }
        return value!!
    }
}