package de.connect2x.lognity.util

import java.lang.Thread as JavaThread

@PublishedApi
internal actual fun getThreadName(): String = JavaThread.currentThread().name