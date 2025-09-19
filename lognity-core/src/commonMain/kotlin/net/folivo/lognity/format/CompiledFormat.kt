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

package net.folivo.lognity.format

import kotlin.jvm.JvmInline

@JvmInline
value class CompiledFormat<T> private constructor(val segments: List<Segment<T>>) {
    companion object {
        fun <T> compile(variables: Map<String, Segment<T>>, pattern: String): CompiledFormat<T> {
            val dfa = DFA()
            for (key in variables.keys) dfa += "{{$key}}"
            val segments = ArrayList<Segment<T>>()
            val textBuffer = StringBuilder()
            for (char in pattern) {
                val match = dfa.next(char)
                if (match == null) {
                    textBuffer.append(char)
                    continue
                }
                textBuffer.deleteRange(textBuffer.length - (match.length - 1), textBuffer.length)
                segments += Text(textBuffer.toString())
                textBuffer.clear()
                segments += variables[match.substring(2..match.lastIndex - 2)]!!
            }
            if (textBuffer.isNotEmpty()) segments += Text(textBuffer.toString())
            // Optimize the compiled format by joining adjacent Text segments
            val optimizedSegments = ArrayList<Segment<T>>()
            for (segment in segments) {
                val lastSegment = optimizedSegments.lastOrNull()
                if (lastSegment is Text && segment is Text) {
                    optimizedSegments.removeLastOrNull()
                    optimizedSegments += Text("${lastSegment.value}${segment.value}")
                    continue
                }
                optimizedSegments += segment
            }
            return CompiledFormat(optimizedSegments)
        }
    }

    sealed interface Segment<T> {
        operator fun invoke(ctx: T): String
    }

    data class Variable<T>(val getter: (T) -> String) : Segment<T> {
        override operator fun invoke(ctx: T): String = getter(ctx)
    }

    data class Text<T>(val value: String) : Segment<T> {
        override operator fun invoke(ctx: T): String = value
    }

    operator fun invoke(ctx: T): String = segments.joinToString("") { segment ->
        segment(ctx)
    }
}