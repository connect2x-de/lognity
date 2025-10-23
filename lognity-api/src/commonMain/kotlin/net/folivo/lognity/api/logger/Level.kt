package net.folivo.lognity.api.logger

import net.folivo.lognity.api.ansi.AnsiBg
import net.folivo.lognity.api.ansi.AnsiFg
import net.folivo.lognity.api.ansi.AnsiSequence
import net.folivo.lognity.api.backend.Backend

/**
 * The log level designates the importance of a logged message in
 * ascending order.
 */
enum class Level( // @formatter:off
    val symbol: String,
    val ansi: AnsiSequence
) { // @formatter:on
    // @formatter:off
    TRACE("\uD83D\uDD0E", AnsiBg.default..AnsiFg.hiPurple),
    DEBUG("\uD83E\uDEB2", AnsiBg.default..AnsiFg.hiGreen),
    INFO ("\uD83D\uDCDC", AnsiBg.default..AnsiFg.default),
    WARN ("\u26A0\uFE0F", AnsiBg.default..AnsiFg.hiYellow),
    ERROR("\uD83D\uDD25", AnsiBg.default..AnsiFg.hiRed),
    FATAL("\uD83D\uDC80", AnsiBg.default..<AnsiFg.hiRed);
    // @formatter:on

    companion object {
        /**
         * Retrieves the default log level as determined by the application.
         *
         * @return The global default log level.
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun default(): Level = Backend.current.defaultLevel
    }
}