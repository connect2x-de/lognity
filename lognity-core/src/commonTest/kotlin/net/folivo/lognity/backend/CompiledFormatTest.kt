/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.backend

import net.folivo.lognity.format.CompiledFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class CompiledFormatTest {
    private val variables: Map<String, CompiledFormat.Segment<String>> = mapOf(
        "foo" to CompiledFormat.Text(", World!"),
        "bar" to CompiledFormat.Variable { ctx -> """🦊 goes $ctx!""" },
        "baz" to CompiledFormat.Variable { ctx -> ctx })

    @Test
    fun `Empty input should result in empty output`() {
        val format = CompiledFormat.compile(variables, "")
        assertEquals("", format(""))
    }

    @Test
    fun `Single text variable gets interpolated correctly`() {
        val format = CompiledFormat.compile(variables, "Hello{{foo}}")
        assertEquals("Hello, World!", format(""))
    }

    @Test
    fun `Single dynamic variable gets interpolated correctly`() {
        val format = CompiledFormat.compile(variables, "The {{bar}}")
        assertEquals("""The 🦊 goes YIP!""", format("YIP"))
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `Complex format gets interpolated correctly`() {
        val format = CompiledFormat.compile<Unit>(mapOf( // @formatter:off
            "yyyy" to CompiledFormat.Variable { "2000" },
            "MM" to CompiledFormat.Variable { "12" },
            "dd" to CompiledFormat.Variable { "07" },
            "hh" to CompiledFormat.Variable { "00" },
            "mm" to CompiledFormat.Variable { "48" },
            "ss" to CompiledFormat.Variable { "12" },
            "SSS" to CompiledFormat.Variable { "223" }
        ), "{{yyyy}}/{{MM}}/{{dd}} {{hh}}:{{mm}}:{{ss}}.{{SSS}}") // @formatter:on
        assertEquals("2000/12/07 00:48:12.223", format(Unit))
    }
}