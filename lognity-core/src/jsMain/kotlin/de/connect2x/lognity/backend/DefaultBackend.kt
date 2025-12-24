package de.connect2x.lognity.backend

import de.connect2x.lognity.api.logger.Level
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@PublishedApi
internal actual fun getDefaultLogLevel(): Level {
    val params = URLSearchParams(window.location.search)
    val levelName = params.get("logLevel")
    return Level.entries.find { it.name.equals(levelName, true) } ?: Level.INFO
}