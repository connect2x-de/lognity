package net.folivo.lognity.util

import co.touchlab.stately.concurrency.ThreadLocalRef

internal class ThreadLocal<T>(
    private val initializer: () -> T
) {
    private val ref: ThreadLocalRef<T> = ThreadLocalRef()

    fun get(): T {
        val oldValue = ref.get()
        return if (oldValue == null) {
            val newValue = initializer()
            ref.set(newValue)
            newValue
        }
        else oldValue
    }
}