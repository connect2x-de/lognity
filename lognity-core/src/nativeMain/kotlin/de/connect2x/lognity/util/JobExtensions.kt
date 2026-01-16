package de.connect2x.lognity.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking

internal actual fun Job.cancelAndJoinBlocking() = runBlocking {
    cancelAndJoin()
}

internal actual fun Job.joinBlocking() = runBlocking {
    join()
}