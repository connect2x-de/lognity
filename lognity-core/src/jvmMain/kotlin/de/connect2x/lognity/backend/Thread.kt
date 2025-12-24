@file:JvmName("ThreadJvm")

package de.connect2x.lognity.backend

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.ptr.LongByReference

private interface LibSystemLinux : Library {
    companion object : LibSystemLinux by Native.load("c", LibSystemLinux::class.java) {
        const val SYS_gettid: Long = 186 // Retrieve the current system thread id
    }

    fun syscall(number: Long, vararg args: Any?): Long
}

private interface LibSystemMacos : Library {
    companion object : LibSystemMacos by Native.load("System", LibSystemMacos::class.java)

    fun pthread_self(): Long
    fun pthread_threadid_np(thread: Long, id: LongByReference): Int
}

internal actual fun getNativeThreadId(): ULong = when {
    Platform.isWindows() -> Kernel32.INSTANCE.GetCurrentThreadId().toULong()
    Platform.isLinux() -> LibSystemLinux.syscall(LibSystemLinux.SYS_gettid).toULong()
    Platform.isMac() -> {
        val id = LongByReference(0)
        LibSystemMacos.pthread_threadid_np(LibSystemMacos.pthread_self(), id)
        id.value.toULong()
    }

    else -> Thread.currentThread().id.toULong() // Fall back to JVM internal ID when nothing else is supported
}