package de.connect2x.lognity.io

import de.connect2x.lognity.util.isNode

internal actual val hasFilesystem: Boolean
    get() = isNode