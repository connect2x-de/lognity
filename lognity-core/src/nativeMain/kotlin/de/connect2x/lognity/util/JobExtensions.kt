package de.connect2x.lognity.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

internal actual fun Job.joinBlocking() = runBlocking { join() }