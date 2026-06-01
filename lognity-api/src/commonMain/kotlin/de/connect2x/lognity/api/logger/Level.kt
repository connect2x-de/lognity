package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.backend.Backend

/**
 * The log level designates the importance of a logged message in
 * ascending order.
 */
enum class Level( // @formatter:off
    val symbol: String
) { // @formatter:on
    // @formatter:off
    TRACE("\uD83D\uDD0E"),
    DEBUG("\uD83E\uDEB2"),
    INFO ("\uD83D\uDCDC"),
    WARN ("\u26A0\uFE0F"),
    ERROR("\uD83D\uDD25"),
    FATAL("\uD83D\uDC80");
    // @formatter:on

    companion object {
        /**
         * Retrieves the default log level as determined by the application.
         */
        inline val default: Level get() = Backend.defaultLevel

        fun byName(name: String): Level? = entries.find { level -> level.name.equals(name, true) }
    }
}
