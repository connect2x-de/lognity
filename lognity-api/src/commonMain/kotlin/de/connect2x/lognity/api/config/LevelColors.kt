package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.ansi.AnsiBg
import de.connect2x.lognity.api.ansi.AnsiFg
import de.connect2x.lognity.api.ansi.AnsiSequence
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.backend.ConsoleColorScheme
import de.connect2x.lognity.api.logger.Level
import kotlin.jvm.JvmInline

/**
 * A mapping between log [Level]s and their corresponding [AnsiSequence] for console output.
 *
 * @property colors The underlying map of levels to ANSI sequences.
 */
@JvmInline
value class LevelColors @PublishedApi internal constructor(val colors: Map<Level, AnsiSequence>) {
    companion object {
        // @formatter:off
        /**
         * Predefined [LevelColors] optimized for light-themed consoles.
         */
        val light: LevelColors = LevelColors(
            Level.TRACE to (AnsiFg.purple on AnsiBg.default),
            Level.DEBUG to (AnsiFg.green on AnsiBg.default),
            Level.INFO to (AnsiFg.default on AnsiBg.default),
            Level.WARN to (AnsiFg.yellow on AnsiBg.default),
            Level.ERROR to (AnsiFg.red on AnsiBg.default),
            Level.FATAL to (AnsiFg.red boldOn AnsiBg.default)
        )

        /**
         * Predefined [LevelColors] optimized for dark-themed consoles.
         */
        val dark: LevelColors = LevelColors(
            Level.TRACE to (AnsiFg.hiPurple on AnsiBg.default),
            Level.DEBUG to (AnsiFg.hiGreen on AnsiBg.default),
            Level.INFO to (AnsiFg.default on AnsiBg.default),
            Level.WARN to (AnsiFg.hiYellow on AnsiBg.default),
            Level.ERROR to (AnsiFg.hiRed on AnsiBg.default),
            Level.FATAL to (AnsiFg.hiRed boldOn AnsiBg.default)
        )
        // @formatter:on

        /**
         * Returns the [LevelColors] that matches the current console color scheme detected by [Backend.consoleColorScheme].
         *
         * @return the optimal [LevelColors] for the current environment.
         */
        fun optimal(): LevelColors = when (Backend.consoleColorScheme) {
            ConsoleColorScheme.DARK -> dark
            ConsoleColorScheme.LIGHT -> light
        }
    }

    internal constructor(vararg pairs: Pair<Level, AnsiSequence>) : this(mapOf(*pairs))

    /**
     * Retrieves the [AnsiSequence] for the given [level].
     *
     * @param level the log level to get the color for.
     * @return the [AnsiSequence] for the given [level], or an empty sequence if not found.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun get(level: Level): AnsiSequence = colors[level] ?: AnsiSequence("")

    /**
     * Overlay the given level colors on the colors in this instance.
     *
     * @param other The colors to overlay on top of the colors from this instance.
     * @return A new colors instance with the combined colors of both instances.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: LevelColors): LevelColors = LevelColors(colors + other.colors)
}
