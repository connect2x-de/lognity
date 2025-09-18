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

package net.folivo.lognity.slf4j

import net.folivo.lognity.api.Marker
import java.util.concurrent.ConcurrentLinkedDeque
import org.slf4j.Marker as Slf4jMarker

class LognitySlf4jMarker(
    val delegate: Marker
) : Slf4jMarker {
    private val children: ConcurrentLinkedDeque<Slf4jMarker> = ConcurrentLinkedDeque()

    override fun getName(): String = delegate.name

    override fun add(reference: Slf4jMarker) {
        if (reference in children) return
        children += reference
    }

    override fun remove(reference: Slf4jMarker): Boolean {
        if (reference !in children) return false
        children -= reference
        return true
    }

    @Deprecated("Deprecated in favour of hasReferences()", replaceWith = ReplaceWith("hasReferences()"))
    override fun hasChildren(): Boolean = children.isNotEmpty()

    override fun hasReferences(): Boolean = children.isNotEmpty()
    override fun iterator(): Iterator<Slf4jMarker> = children.iterator()
    override fun contains(other: Slf4jMarker): Boolean = other in children
    override fun contains(name: String): Boolean = children.any { it.name == name }
}

fun Marker.asSlf4jMarker(): Slf4jMarker = LognitySlf4jMarker(this)

fun Slf4jMarker.asLognityMarker(): Marker = when (this) {
    is LognitySlf4jMarker -> delegate
    else -> Marker(name)
}