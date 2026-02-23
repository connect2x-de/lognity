@file:JvmName("ThreadAndroid")

package de.connect2x.lognity.backend

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform

private interface LibSystemAndroid : Library {
    companion object {
        // Syscall ID for the gettid syscall to retrieve an actual system thread ID
        //  arm64/riscv64: https://github.com/torvalds/linux/blob/master/scripts/syscall.tbl
        //  arm32: https://github.com/torvalds/linux/blob/master/arch/arm/tools/syscall.tbl
        //  x86_64: https://github.com/torvalds/linux/blob/master/arch/x86/entry/syscalls/syscall_64.tbl
        //  x86: https://github.com/torvalds/linux/blob/master/arch/x86/entry/syscalls/syscall_32.tbl
        val SYS_gettid: Long = when {
            Platform.isIntel() -> if (Platform.is64Bit()) 186L else 224L
            Platform.isARM() -> if (Platform.is64Bit()) 178L else 224L
            Platform.isRISCV() -> 178L
            else -> error("Unsupported Android target architecture")
        }
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