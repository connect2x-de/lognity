package de.connect2x.lognity.util

import kotlinx.coroutines.Job

internal expect fun Job.cancelAndJoinBlocking()

internal expect fun Job.joinBlocking()