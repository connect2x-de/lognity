@file:JvmName("ThreadAndroid")

package de.connect2x.lognity.backend

import com.sun.jna.Library
import com.sun.jna.Native

private interface LibSystemAndroid : Library {
    companion object : LibSystemAndroid by Native.load("c", LibSystemAndroid::class.java) {
        const val SYS_gettid: Long = 186 // Retrieve the current system thread id
    }

    fun syscall(number: Long, vararg args: Any?): Long
}

internal actual fun getNativeThreadId(): ULong {
    return LibSystemAndroid.syscall(LibSystemAndroid.SYS_gettid).toULong()
}