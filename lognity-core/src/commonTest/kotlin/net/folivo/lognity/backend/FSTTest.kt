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

import net.folivo.lognity.format.FST
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FSTTest {
    private var dynamicValue: () -> String = { "foo" }

    @OptIn(ExperimentalTime::class)
    private val transforms: Map<String, (Unit) -> String> = mapOf(
        "{{timeMillis}}" to { Clock.System.now().toEpochMilliseconds().toString() },
        "{{threadName}}" to { getThreadName() },
        "{{threadId}}" to { getThreadId().toString() },
        "{{test}}" to { "World!" },
        "{{dynamic}}" to { dynamicValue() })

    @Test
    fun `Empty string results in empty output`() {
        val fst = FST(transforms)
        assertEquals("", fst("", Unit))
    }

    @Test
    fun `One variable gets transformed`() {
        val fst = FST(transforms)
        assertEquals("Hello, World!", fst("Hello, {{test}}", Unit))
        dynamicValue = { "foo" }
        assertEquals("Hello, foo", fst("Hello, {{dynamic}}", Unit))
        dynamicValue = { "bar" }
        assertEquals("Hello, bar", fst("Hello, {{dynamic}}", Unit))
    }

    @Test
    fun `Multiple variables get transformed`() {
        val fst = FST(transforms)
        assertEquals("Hello, World! - foo", fst("Hello, {{test}} - {{dynamic}}", Unit))
        dynamicValue = { "foo" }
        assertEquals("Hello, fooWorld!", fst("Hello, {{dynamic}}{{test}}", Unit))
        dynamicValue = { "bar" }
        assertEquals("Hello, barWorld!", fst("Hello, {{dynamic}}{{test}}", Unit))
    }
}