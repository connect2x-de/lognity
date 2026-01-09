package de.connect2x.lognity.backend

import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.util.isNode
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@PublishedApi
internal actual fun getDefaultLogLevel(): Level {
    if (isNode) return Level.INFO // TODO: find a way to implement this for node, possibly using passed in args or smth
    val params = URLSearchParams(window.location.search)
    val levelName = params.get("logLevel")
    return Level.entries.find { it.name.equals(levelName, true) } ?: Level.INFO
}