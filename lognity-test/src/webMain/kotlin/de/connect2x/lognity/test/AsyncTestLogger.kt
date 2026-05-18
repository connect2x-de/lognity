package de.connect2x.lognity.test

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.marker.Marker
import de.connect2x.lognity.logger.DefaultLogger

internal actual class AsyncTestLogger actual constructor( // @formatter:off
    config: Config,
    context: Context
) : DefaultLogger(config, context) { // @formatter:on
    actual override fun log(level: Level, message: MessageProvider) {
        if (!isKarma) {
            super.log(level, message)
            return
        }
        super.log(level) { "${message()}\n" }
    }

    actual override fun log(marker: Marker?, level: Level, message: MessageProvider) {
        if (!isKarma) {
            super.log(marker, level, message)
            return
        }
        super.log(marker, level) { "${message()}\n" }
    }
}