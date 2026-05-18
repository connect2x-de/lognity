package de.connect2x.lognity.api.logger

/**
 * A closure which evaluates to any value used as the content of a log message.
 * Provides this [Logger] instance as a context parameter and uses
 * [MessageScope] as its receiver type.
 */
typealias MessageProvider = context(Logger) MessageScope.() -> Any?

/**
 * Convenience overload that automatically provides the [MessageScope] instance.
 */
operator fun MessageProvider.invoke(logger: Logger): Any? = this(logger, MessageScope)