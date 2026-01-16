package de.connect2x.lognity.util

import kotlinx.coroutines.Job

internal actual fun Job.cancelAndJoinBlocking() = Unit

internal actual fun Job.joinBlocking() = Unit