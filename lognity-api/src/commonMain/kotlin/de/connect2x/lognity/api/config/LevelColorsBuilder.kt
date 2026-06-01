package de.connect2x.lognity.api.config

import de.connect2x.lognity.api.ansi.AnsiSequence
import de.connect2x.lognity.api.logger.Level
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A builder class for creating [LevelColors] using a DSL.
 */
@ConfigDsl
class LevelColorsBuilder @PublishedApi internal constructor() {
    private val colors: HashMap<Level, AnsiSequence> = HashMap()

    init {
        setFrom(LevelColors.optimal()) // Default to current optimal colors
    }

    /**
     * Copies all level-color mappings from the given [colors] into this builder.
     *
     * @param colors the [LevelColors] to copy mappings from.
     */
    fun setFrom(colors: LevelColors) {
        this.colors += colors.colors
    }

    /**
     * Copies all level-color mappings from the given [colors] map into this builder.
     *
     * @param colors the map of [Level] to [AnsiSequence] to copy mappings from.
     */
    fun setFrom(colors: Map<Level, AnsiSequence>) {
        this.colors += colors
    }

    /**
     * Sets the [color] for the specified [level].
     *
     * @param level the log level.
     * @param color the ANSI sequence to use for the level.
     */
    fun color(level: Level, color: AnsiSequence) {
        colors[level] = color
    }

    /**
     * Sets the color for the [Level.TRACE] level.
     *
     * @param color the ANSI sequence to use.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun trace(color: AnsiSequence) = color(Level.TRACE, color)

    /**
     * Sets the color for the [Level.DEBUG] level.
     *
     * @param color the ANSI sequence to use.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun debug(color: AnsiSequence) = color(Level.DEBUG, color)

    /**
     * Sets the color for the [Level.INFO] level.
     *
     * @param color the ANSI sequence to use.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun info(color: AnsiSequence) = color(Level.INFO, color)

    /**
     * Sets the color for the [Level.WARN] level.
     *
     * @param color the ANSI sequence to use.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun warn(color: AnsiSequence) = color(Level.WARN, color)

    /**
     * Sets the color for the [Level.ERROR] level.
     *
     * @param color the ANSI sequence to use.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun error(color: AnsiSequence) = color(Level.ERROR, color)

    /**
     * Sets the color for the [Level.FATAL] level.
     *
     * @param color the ANSI sequence to use.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun fatal(color: AnsiSequence) = color(Level.FATAL, color)

    @PublishedApi
    internal fun build(): LevelColors = LevelColors(colors)
}

/**
 * Type alias for a [LevelColorsBuilder] specification.
 */
typealias LevelColorsSpec = LevelColorsBuilder.() -> Unit

/**
 * Creates [LevelColors] using the provided [spec].
 *
 * @param spec the builder specification.
 * @return the created [LevelColors].
 */
inline fun LevelColors(spec: LevelColorsSpec): LevelColors {
    contract {
        callsInPlace(spec, InvocationKind.EXACTLY_ONCE)
    }
    return LevelColorsBuilder().apply(spec).build()
}
