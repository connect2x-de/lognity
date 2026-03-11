package de.connect2x.lognity.config.extension

/**
 * Interface for providing extensions to the Lognity configuration.
 *
 * This allows registering custom appenders and conditions that can be used within the configuration.
 */
fun interface ConfigExtension {
    /**
     * Registers the extension's components using the provided [ConfigExtensionRegistrar].
     */
    fun ConfigExtensionRegistrar.register()
}