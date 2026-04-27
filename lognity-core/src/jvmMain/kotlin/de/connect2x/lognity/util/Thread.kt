@file:JvmName("ThreadJvm")

package de.connect2x.lognity.util

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.ptr.LongByReference
import java.lang.Thread as JavaThread

private interface LibSystemLinux : Library {
    companion object : LibSystemLinux by Native.load("c", LibSystemLinux::class.java) {
        // Syscall ID for the gettid syscall to retrieve an actual system thread ID
        //  arm64/riscv64: https://github.com/torvalds/linux/blob/master/scripts/syscall.tbl
        //  arm32: https://github.com/torvalds/linux/blob/master/arch/arm/tools/syscall.tbl
        //  x86_64: https://github.com/torvalds/linux/blob/master/arch/x86/entry/syscalls/syscall_64.tbl
        //  x86: https://github.com/torvalds/linux/blob/master/arch/x86/entry/syscalls/syscall_32.tbl
        val SYS_gettid: Long = when {
            Platform.isIntel() -> if (Platform.is64Bit()) 186L else 224L
            Platform.isARM() -> if (Platform.is64Bit()) 178L else 224L
            Platform.isRISCV() -> 178L
            else -> error("Unsupported JVM target architecture")
        }
    }

    fun syscall(number: Long, vararg args: Any?): Long
}

private interface LibSystemMacos : Library {
    companion object : LibSystemMacos by Native.load("System", LibSystemMacos::class.java)

    fun pthread_self(): Long
    fun pthread_threadid_np(thread: Long, id: LongByReference): Int
}

internal actual fun getThreadId(): ULong = when {
    Platform.isWindows() -> Kernel32.INSTANCE.GetCurrentThreadId().toULong()
    Platform.isLinux() -> LibSystemLinux.syscall(LibSystemLinux.SYS_gettid).toULong()
    Platform.isMac() -> {
        val id = LongByReference(0)
        LibSystemMacos.pthread_threadid_np(LibSystemMacos.pthread_self(), id)
        id.value.toULong()
    }

    else -> JavaThread.currentThread().id.toULong() // Fall back to JVM internal ID when nothing else is supported
}