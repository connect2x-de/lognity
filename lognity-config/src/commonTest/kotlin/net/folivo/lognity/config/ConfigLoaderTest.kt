package net.folivo.lognity.config

import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import net.folivo.lognity.api.backend.Backend
import net.folivo.lognity.api.logger.Level
import net.folivo.lognity.appender.FileAppender
import net.folivo.lognity.backend.DefaultBackend
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ConfigLoaderTest {
    @BeforeTest
    fun setup() {
        Backend.current = DefaultBackend
    }

    @Test
    fun `Empty file creates default config`() {
        val emptySource = Buffer()
        emptySource.writeString("{}")
        val config = SerializableConfig.load(emptySource).createConfig()
        assertEquals(Level.default(), config.initialLevel)
        assertTrue(config.initialEnableState)
        assertTrue(config.appenders.isEmpty())
    }

    @Test
    fun `Initial level is set correctly`() {
        val source = Buffer()
        source.writeString(
            """
            {
              "level": "TRACE" // Enable tracing for all created logger by default
            }
        """.trimIndent()
        )
        val config = SerializableConfig.load(source).createConfig()
        assertEquals(Level.TRACE, config.initialLevel)
    }

    @Test
    fun `Initial enable state is set correctly`() {
        val source = Buffer()
        source.writeString(
            """
            {
              "enabled": true // Enable all created logger by default
            }
        """.trimIndent()
        )
        val config = SerializableConfig.load(source).createConfig()
        assertTrue(config.initialEnableState)
    }

    @Test
    fun `Console appender is parsed correctly`() {
        val source = Buffer()
        source.writeString(
            """
            {
              "appenders": [
                {
                  "type": "console",
                  "pattern": "{{message}}",
                  "formatter": "default",
                  "filter": {
                    "conditions": [
                      {
                        "type": "message",
                        "condition": "NOT_CONTAINS",
                        "value": "YOPYOPYOP"
                      }
                    ]
                  }
                }
              ]   
            }
        """.trimIndent()
        )
        val config = SerializableConfig.load(source).createConfig()
        val appender = config.appenders.first()
        assertEquals("{{message}}", appender.pattern)
        assertSame(Backend.current.defaultFormatter, appender.formatter)
    }

    @Test
    fun `File appender is parsed correctly`() {
        val source = Buffer()
        source.writeString(
            """
            {
              "appenders": [
                {
                  "type": "file",
                  "path": "latest.log", // May be absolute or relative to the executable path
                  "pattern": "{{message}}",
                  "formatter": "default",
                  "filter": {
                    "conditions": [
                      {
                        "type": "message",
                        "condition": "NOT_CONTAINS",
                        "value": "YOPYOPYOP"
                      }
                    ]
                  }
                }
              ]    
            }
        """.trimIndent()
        )
        val config = SerializableConfig.load(source).createConfig()
        val appender = config.appenders.first()
        if (appender !is FileAppender) return
        assertEquals("{{message}}", appender.pattern)
        assertEquals(Path("latest.log"), appender.path)
        assertSame(Backend.current.defaultFormatter, appender.formatter)
    }
}