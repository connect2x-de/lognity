package net.folivo.lognity.backend

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import platform.linux.PR_GET_NAME
import platform.linux.SYS_gettid
import platform.linux.prctl
import platform.posix.PATH_MAX
import platform.posix.syscall

private const val MAX_NAME_LENGTH: Int = PATH_MAX

@OptIn(ExperimentalForeignApi::class)
internal actual fun getThreadName(): String = memScoped {
    val name = allocArray<ByteVar>(MAX_NAME_LENGTH)
    prctl(PR_GET_NAME, name, 0, 0, 0)
    name.toKStringFromUtf8()
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getNativeThreadId(): ULong = syscall(SYS_gettid.convert()).toULong()