package de.connect2x.lognity.api.sanitization

import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageScope

/**
 * Converts the given value into a string using [Any.toString]
 * and sanitizes the result, based on the [Logger] configuration.
 *
 * If [de.connect2x.lognity.api.config.Config.sanitizationMode] is [SanitizationMode.DISABLED],
 * the value is taken as is and left unmodified.
 * When it is [SanitizationMode.OBFUSCATE] (which is the global default),
 * the value is converted into an obfuscated placeholder of the same length made of '*' asterisks.
 * If the mode is [SanitizationMode.HIDE], the value is omitted completely from the log output,
 * for information where even revealing the length is considered too sensitive.
 *
 * @param value The value to sanitize on demand if needed.
 * @return A sanitized version of the passed in value, depending
 *  on the current [Logger.config].
 */
context(logger: Logger, _: MessageScope)
fun secret(value: Any?): String {
    val rawString = value.toString()
    return when (logger.config.sanitizationMode) {
        SanitizationMode.OBFUSCATE -> "*".repeat(rawString.length)
        SanitizationMode.OBFUSCATE_FIXED -> "***"
        SanitizationMode.HIDE -> ""
        else -> rawString
    }
}