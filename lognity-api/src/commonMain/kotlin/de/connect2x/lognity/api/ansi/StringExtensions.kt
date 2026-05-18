package de.connect2x.lognity.api.ansi

import de.connect2x.lognity.api.logger.MessageScope

/** Prefix this string with the given [AnsiSequence]. */
@Suppress("NOTHING_TO_INLINE")
context(_: MessageScope) inline infix fun String.with(sequence: AnsiSequence): AnsiString = AnsiString("$sequence$this")

/** Append [AnsiSequence.reset] after this string. */
@Suppress("NOTHING_TO_INLINE")
context(_: MessageScope) inline fun String.reset(): AnsiString = AnsiString("$this${AnsiSequence.reset}")