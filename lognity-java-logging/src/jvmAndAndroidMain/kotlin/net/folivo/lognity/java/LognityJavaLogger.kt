package net.folivo.lognity.java

import net.folivo.lognity.api.logger.Logger
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger as JavaLogger

private class LognityJavaLogger(
    private val delegate: Logger
) : JavaLogger(delegate.context[Logger.Name]?.name ?: delegate.toString(), null) {
    override fun getLevel(): Level = delegate.level.asJavaLevel()

    override fun setLevel(newLevel: Level?) {
        delegate.level = newLevel?.asLognityLevel() ?: return
    }

    override fun log(record: LogRecord) {
        delegate.log(record.level.asLognityLevel()) { record.message }
    }
}

/**
 * Expose this Lognity [Logger] as a [java.util.logging.Logger] (JUL) instance.
 *
 * The returned JUL logger delegates to the underlying Lognity logger:
 * - Reading the level via [JavaLogger.getLevel] reflects the current Lognity level.
 * - Setting the level via [JavaLogger.setLevel] updates the Lognity level using the mapping in [asLognityLevel].
 * - Logging a [java.util.logging.LogRecord] forwards the call to Lognity with the mapped level via [asLognityLevel].
 *
 * This is useful when you need to pass a JUL logger to libraries expecting `java.util.logging.Logger`,
 * while still routing logs through Lognity.
 */
fun Logger.asJavaLogger(): JavaLogger = LognityJavaLogger(this)