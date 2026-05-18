package de.connect2x.lognity.api.sanitization

import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.context.Context
import de.connect2x.lognity.api.context.EmptyContext
import de.connect2x.lognity.api.logger.Level
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.api.logger.MessageProvider
import de.connect2x.lognity.api.logger.MessageScope
import de.connect2x.lognity.api.marker.Marker
import kotlin.test.Test
import kotlin.test.assertEquals

class SecretTest {
    private class TestLogger(override val config: Config) : Logger {
        override val context: Context = EmptyContext
        override var level: Level = Level.INFO
        override var isEnabled: Boolean = true
        override fun log(level: Level, message: MessageProvider) = Unit
        override fun log(marker: Marker?, level: Level, message: MessageProvider) = Unit
        override fun flush() = Unit
    }

    @Test
    fun testSecretDisabled() {
        val logger = TestLogger(Config(sanitizationMode = SanitizationMode.DISABLED))
        val value = "my-secret-password"
        with(logger) {
            with(MessageScope) {
                assertEquals(value, secret(value))
            }
        }
    }

    @Test
    fun testSecretObfuscate() {
        val logger = TestLogger(Config(sanitizationMode = SanitizationMode.OBFUSCATE))
        val value = "password"
        with(logger) {
            with(MessageScope) {
                assertEquals("********", secret(value))
            }
        }
    }

    @Test
    fun testSecretHide() {
        val logger = TestLogger(Config(sanitizationMode = SanitizationMode.HIDE))
        val value = "sensitive"
        with(logger) {
            with(MessageScope) {
                assertEquals("", secret(value))
            }
        }
    }

    @Test
    fun testSecretWithNull() {
        val logger = TestLogger(Config(sanitizationMode = SanitizationMode.OBFUSCATE))
        with(logger) {
            with(MessageScope) {
                assertEquals("****", secret(null)) // "null".length == 4
            }
        }
    }

    @Test
    fun testSecretEmptyString() {
        val logger = TestLogger(Config(sanitizationMode = SanitizationMode.OBFUSCATE))
        with(logger) {
            with(MessageScope) {
                assertEquals("", secret(""))
            }
        }
    }
}