package de.connect2x.lognity.api.backend

/**
 * An enumeration defining all commonly supported platforms Lognity may be used on.
 */
enum class Platform {
    WINDOWS, LINUX, MACOS, IOS, ANDROID, WEB,

    // All targets not listed here on the JVM will return this platform. If you're missing a platform, feel free to add it.
    UNKNOWN_JVM,

    // This shall only ever be used by noop backends
    UNKNOWN
}