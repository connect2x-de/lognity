package de.connect2x.lognity.backend

import de.connect2x.lognity.api.logger.Level

@PublishedApi
internal actual fun getDefaultLogLevel(): Level = Level.INFO // TODO: implement a way to change this