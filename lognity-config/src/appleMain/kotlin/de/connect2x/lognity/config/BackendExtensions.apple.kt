package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes

@PublishedApi
@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        byteArray.usePinned { pinned ->
            getBytes(pinned.addressOf(0), length)
        }
    }
    return byteArray
}

actual suspend fun Backend.withDefaultConfig(path: String, block: suspend () -> Unit) {
    val split = path.split(".")
    val fileEnding = if (split.size > 1) split.last() else null

    val bundle = NSBundle.mainBundle
    val path = bundle.pathForResource(split[0], fileEnding) ?: error("Unable to locate logger config '$path'")
    val data = NSData.dataWithContentsOfFile(path)?.toByteArray() ?: error("Unable to read logger config")
    Backend.setDefaultConfig(Buffer().also { it.write(data) })
    block()
}
