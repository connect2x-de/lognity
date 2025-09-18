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

import kotlin.text.iterator

/**
 * A simple generic finite state transducer implementation for efficiently transforming the
 * log message strings given a specific template.
 * This state machine allows us to lazily parse all string template variables
 * instead of greedily looping over all characters for every possible variable.
 *
 * The transducer in this non-standard implementation uses three "tapes" (input, staging and output buffers).
 * It reads the input tape while maintaining a DFA, which continuously matches the input
 * characters against all possible template variables using a sliding window.
 *
 * As long as the DFA indicates that no match is present, characters from the input tape
 * are streamed directly through to the staging tape.
 *
 * However, if the DFA indicates that a match is present, the staging tape's content is
 * flushed to the output tape, which is followed by the translated value of the DFA match
 * (a template variable in the log pattern).
 *
 * Once the input tape reaches the end, the staging buffer is flushed to the
 * output tape one more time to ensure any remaining staging content is also
 * added to the result.
 */
internal class FST<T>( // @formatter:off
    val dfa: DFA,
    val transform: (String, T) -> String
) { // @formatter:on
    constructor(transforms: Map<String, (T) -> String>) : this(DFA().apply {
        transforms.keys.forEach { key -> this += key }
    }, { key, ctx -> transforms[key]!!(ctx) })

    operator fun invoke(value: String, ctx: T): String {
        val staging = StringBuilder()
        val output = StringBuilder()
        for (char in value) {
            val match = dfa.next(char)
            if (match == null) {
                staging.append(char)
                continue
            }
            // Remove the match itself from the end of the staging buffer
            staging.deleteRange(staging.length - (match.length - 1), staging.length)
            // Flush staging buffer contents to output buffer
            output.append(staging)
            // Clear the staging buffer
            staging.clear()
            // Apply the match transform and emit the transformed value directly into the output
            output.append(transform(match, ctx))
        }
        if (staging.isNotEmpty()) {
            // Flush any remaining contents if present
            output.append(staging)
        }
        return output.toString()
    }
}