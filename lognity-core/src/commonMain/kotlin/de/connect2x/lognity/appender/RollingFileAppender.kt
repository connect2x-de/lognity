package de.connect2x.lognity.appender

import de.connect2x.lognity.api.appender.Filter
import de.connect2x.lognity.api.format.Formatter
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class RollingFileAppender(
    pattern: String, formatter: Formatter, filter: Filter, private val basePath: Path, name: String? = null
) : FileAppender(pattern, formatter, filter, suffixFileName(basePath, "latest"), name) {
    companion object {
        private fun suffixFileName(path: Path, suffix: String): Path {
            val fileName = path.name
            val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'))
            val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
            val parentPath = path.parent ?: Path("")
            return Path(parentPath, "$fileNameWithoutExt-$suffix.$fileExt")
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun afterAggregatorShutdown() {
        sink.release {
            sinks -= path
            val timestamp = Clock.System.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
            val timestampedPath = suffixFileName(basePath, timestamp)
            SystemFileSystem.atomicMove(path, timestampedPath) // Rename the latest file to the timestamped name
        }
    }
}