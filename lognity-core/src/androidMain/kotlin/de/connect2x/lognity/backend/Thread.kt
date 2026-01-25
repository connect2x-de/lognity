@file:JvmName("ThreadAndroid")

package de.connect2x.lognity.backend

import com.sun.jna.Library
import com.sun.jna.Native

private interface LibSystemAndroid : Library {
    companion object {
        const val SYS_gettid: Long = 186 // Retrieve the current system thread id
    }

    fun syscall(number: Long, vararg args: Any?): Long
}

// Attempt to load native android library and fail silently
private val libSystemAndroid: LibSystemAndroid? = try {
    Native.load("c", LibSystemAndroid::class.java)
} catch (_: Throwable) {
    null
}

@Suppress("DEPRECATION") // Thread.id is deprecated on desktop Java, but not Android
internal actual fun getNativeThreadId(): ULong {
    // If the system lib cannot be loaded, we are in a mocked environment, use regular JVM thread ID.
    return (libSystemAndroid?.syscall(LibSystemAndroid.SYS_gettid) ?: Thread.currentThread().id).toULong()
}