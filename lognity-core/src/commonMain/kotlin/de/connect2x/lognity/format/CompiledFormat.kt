package de.connect2x.lognity.format

import de.connect2x.lognity.format.CompiledFormat.Companion.compile
import kotlin.jvm.JvmInline

/**
 * A compiled, ready-to-render format made of immutable segments.
 *
 * The format is compiled from a pattern containing placeholders (e.g. "{{name}}"),
 * where each placeholder resolves to a [Segment] that can produce text from a
 * provided context of type [T].
 *
 * This class is a value class that holds the optimized list of [segments]. Use
 * [compile] to build an instance from a pattern and a map of variables.
 *
 * Type parameter [T] represents the rendering context passed to segments when
 * producing the final string.
 */
@JvmInline
value class CompiledFormat<T> private constructor(val segments: List<Segment<T>>) {
    companion object {
        /**
         * Compiles a [pattern] into a [CompiledFormat] by resolving placeholders to variables.
         *
         * Placeholders in the pattern must be written using double curly braces surrounding the
         * variable name, e.g. `{{variable}}`. For each placeholder, a corresponding entry must
         * exist in [variables], mapping the variable name to a [Segment] (typically [Variable]).
         *
         * Compilation performs a simple single-pass recognition of placeholders and optimizes the
         * result by merging adjacent [Text] segments. Unknown variables will throw via `requireNotNull` when
         * encountered during compilation.
         *
         * Example:
         * - variables = mapOf("name" to Variable { it.userName })
         * - pattern = "Hello, {{name}}!"
         *
         * @param variables mapping from variable name to [Segment] used to render it.
         * @param pattern the pattern that may contain placeholders like `{{name}}`.
         * @return an optimized [CompiledFormat] ready to be invoked with a context.
         */
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
                segments += requireNotNull(variables[match.substring(2..match.lastIndex - 2)])
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
        /**
         * Renders this segment using the given [ctx] context.
         *
         * @param ctx the rendering context providing values used by this segment
         * @return the textual representation of this segment for the given [ctx]
         */
        operator fun invoke(ctx: T): String
    }

    /**
     * A [Segment] that renders a dynamic value by invoking [getter] with the context.
     *
     * @param getter function that extracts or computes the string value from the given context [T].
     */
    @JvmInline
    value class Variable<T>(val getter: (T) -> String) : Segment<T> {
        override operator fun invoke(ctx: T): String = getter(ctx)
    }

    /**
     * A [Segment] that renders static text regardless of the context.
     *
     * @param value the literal text value that will be emitted during rendering.
     */
    @JvmInline
    value class Text<T>(val value: String) : Segment<T> {
        override operator fun invoke(ctx: T): String = value
    }

    /**
     * Renders the compiled format by invoking each [Segment] with the given [ctx]
     * and concatenating their results.
     *
     * @param ctx the rendering context passed to every segment
     * @return the final rendered string
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun invoke(ctx: T): String = segments.joinToString("") { segment ->
        segment(ctx)
    }
}