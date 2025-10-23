package net.folivo.lognity.backend

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.posix.atexit

@OptIn(ExperimentalForeignApi::class)
internal actual object ShutdownHandler {
    private val hooks: ArrayList<() -> Unit> = ArrayList()
    private val mutex: Mutex = Mutex()

    init {
        atexit(staticCFunction<Unit> {
            // Need full qualifier here because of volatile closure
            ShutdownHandler.invokeAll()
        })
    }

    private fun invokeAll() {
        runBlocking {
            mutex.withLock {
                for (hook in hooks) hook()
            }
        }
    }

    actual fun register(block: () -> Unit) {
        runBlocking {
            mutex.withLock {
                hooks += block
            }
        }
    }
}