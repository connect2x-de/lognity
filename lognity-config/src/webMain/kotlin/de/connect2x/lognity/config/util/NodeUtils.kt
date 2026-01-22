package de.connect2x.lognity.config.util

import kotlin.js.js

private fun checkIsNode(): Boolean = js("""typeof process !== "undefined" && process.release.name === "node"""")

@PublishedApi
internal val isNode: Boolean = checkIsNode()