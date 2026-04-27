package de.connect2x.lognity.config

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.appender.FileAppender
import de.connect2x.lognity.backend.DefaultBackend
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class FileAppenderConfigTest {
    @BeforeTest
    fun setup() {
        Backend.setOnce(DefaultBackend)
        SerializableConfig uses CoreConfigExtension
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
                  "platforms": ["LINUX", "WINDOWS", "MACOS", "UNKNOWN_JVM"],
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
        val config = SerializableConfig.load(source).asConfig()
        // If this is null, appender is not applicable to this platform
        val appender = config.appenders.firstOrNull() ?: return
        if (appender !is FileAppender) return
        assertEquals("{{message}}", appender.pattern)
        assertEquals(Path("latest.log"), appender.path)
        assertSame(Backend.defaultFormatter, appender.formatter)
    }
}