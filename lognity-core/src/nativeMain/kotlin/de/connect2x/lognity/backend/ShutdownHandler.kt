package de.connect2x.lognity.backend

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.posix.atexit

@OptIn(ExperimentalForeignApi::class)
internal actual object ShutdownHandler {
    private val hooks: ArrayList<Pair<() -> Unit, Int>> = ArrayList()
    private val mutex: Mutex = Mutex()

    init {
        atexit(staticCFunction<Unit> {
            // Need full qualifier here because of volatile closure
            val handler = ShutdownHandler // To prevent formatter from removing ref
            handler.shutdown()
        })
    }

    private fun shutdown() {
        runBlocking {
            mutex.withLock {
                for ((hook, _) in hooks.sortedBy(Pair<() -> Unit, Int>::second)) hook()
            }
        }
    }

    actual fun register(block: () -> Unit, priority: Int) {
        runBlocking {
            mutex.withLock {
                hooks += block to priority
            }
        }
    }
}