package net.folivo.lognity.backend

import net.folivo.lognity.api.logger.Level
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
@PublishedApi
internal actual fun getDefaultLogLevel(): Level {
    return if (Platform.isDebugBinary) Level.DEBUG else Level.INFO
}