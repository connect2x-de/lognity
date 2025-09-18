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

import net.folivo.lognity.format.DFA
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DFATest {
    @Test
    fun `Incomplete input doesnt match`() {
        val dfa = DFA()
        dfa += "match"
        dfa += "make"

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('t'))
        assertNull(dfa.next('c'))
    }

    @Test
    fun `Partial match resets state`() {
        val dfa = DFA()
        dfa += "match"
        dfa += "make"

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('t'))
        assertNull(dfa.next('c'))
        assertNull(dfa.next('a')) // This resets state
        assertNull(dfa.next('h')) // This doesn't complete the match anymore
    }

    @Test
    fun `Complete input matches once`() {
        val dfa = DFA()
        dfa += "match"
        dfa += "make"

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('t'))
        assertNull(dfa.next('c'))
        assertEquals("match", dfa.next('h'))
    }

    @Test
    fun `Complete input matches more than once`() {
        val dfa = DFA()
        dfa += "match"
        dfa += "make"

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('t'))
        assertNull(dfa.next('c'))
        assertEquals("match", dfa.next('h'))

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('k'))
        assertEquals("make", dfa.next('e'))
    }

    @Test
    fun `Complete input matches after reset`() {
        val dfa = DFA()
        dfa += "match"
        dfa += "make"

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('t'))
        assertNull(dfa.next('c'))
        assertNull(dfa.next('a')) // This resets state

        assertNull(dfa.next('m'))
        assertNull(dfa.next('a'))
        assertNull(dfa.next('t'))
        assertNull(dfa.next('c'))
        assertEquals("match", dfa.next('h'))
    }
}