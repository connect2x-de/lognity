package de.connect2x.lognity.util

import kotlinx.browser.window

internal val isChrome: Boolean = !isNode && "chrome" in window.navigator.userAgent.lowercase()