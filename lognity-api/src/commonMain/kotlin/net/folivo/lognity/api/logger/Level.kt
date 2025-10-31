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
    TRACE("\uD83D\uDD0E", AnsiFg.hiPurple on AnsiBg.default),
    DEBUG("\uD83E\uDEB2", AnsiFg.hiGreen on AnsiBg.default),
    INFO ("\uD83D\uDCDC", AnsiFg.default on AnsiBg.default),
    WARN ("\u26A0\uFE0F", AnsiFg.hiYellow on AnsiBg.default),
    ERROR("\uD83D\uDD25", AnsiFg.hiRed on AnsiBg.default),
    FATAL("\uD83D\uDC80", AnsiFg.hiRed boldOn AnsiBg.default);
    // @formatter:on

    companion object {
        /**
         * Retrieves the default log level as determined by the application.
         */
        inline val default: Level get() = Backend.defaultLevel
    }
}