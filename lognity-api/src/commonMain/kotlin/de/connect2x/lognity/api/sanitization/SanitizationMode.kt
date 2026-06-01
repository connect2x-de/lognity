package de.connect2x.lognity.api.sanitization

enum class SanitizationMode {
    // @formatter:off
    DISABLED,        // Every secret will be shown as plaintext in the logs
    OBFUSCATE,       // Every secret will be replaced with '*' asterisks
    OBFUSCATE_FIXED, // Every secret will be replaced with "***"
    HIDE             // Every secret will be omitted from the logs completely
    // @formatter:on
}