package de.connect2x.lognity.config

import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RefOrValueTest {
    @Test
    fun `Multiple references at string boundaries are interpolated`() {
        val value = Json.decodeFromString<RefOrValue<String>>(""""{host}:{port}"""")
        assertEquals(
            listOf(RefOrValue.Ref("host"), RefOrValue.Value(":"), RefOrValue.Ref("port")),
            assertIs<RefOrValue.LerpedString>(value).segments
        )
    }

    @Test
    fun `Bare references templates and escaped patterns retain their meaning`() {
        assertEquals(RefOrValue.Ref("host"), Json.decodeFromString<RefOrValue<String>>(""""{host}""""))
        assertEquals(
            RefOrValue.TemplateRef("conditions", "accepted"),
            Json.decodeFromString<RefOrValue<String>>(""""{conditions:accepted}"""")
        )
        assertEquals(
            RefOrValue.Value("{{message}}"),
            Json.decodeFromString<RefOrValue<String>>(""""{{message}}"""")
        )
    }

    @Test
    fun `Quoted closing braces inside template arguments do not end the reference`() {
        val value = Json.decodeFromString<RefOrValue<String>>(""""{templates:example('a}b')}"""")
        val template = assertIs<RefOrValue.TemplateRef<String>>(value)
        assertEquals("templates", template.prefix)
        assertEquals("example", template.name)
        assertEquals(listOf(RefOrValue.Value("a}b")), template.parameters)
    }
}