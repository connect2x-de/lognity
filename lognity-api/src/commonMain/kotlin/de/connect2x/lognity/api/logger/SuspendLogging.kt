package de.connect2x.lognity.api.logger

import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.api.context.ContextSpec
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext

/**
 * Creates a new [Logger] that derives from this instance within a coroutine.
 *
 * This function is similar to [Logger.derive], but it is a suspend function and automatically
 * propagates the current [CoroutineName] from the [currentCoroutineContext] to the new logger's
 * context if it is present.
 *
 * The derived logger keeps the same name as this logger (if any) and starts with a copy
 * of this logger's Context. The provided [contextSpec] is then applied on top, allowing you
 * to add or override context values for the derived instance without mutating the original.
 *
 * @param contextSpec A Context builder that augments the copied context for the derived logger.
 * @return A new Logger sharing the same name and configuration, with an extended Context including the coroutine name.
 */
suspend inline fun Logger.deriveSuspend(crossinline contextSpec: ContextSpec = {}): Logger {
    val coroutineContext = currentCoroutineContext()
    val name = context[Logger.Name]?.name
    val coroutineName = coroutineContext[CoroutineName]?.name
    return Backend.createLogger(name) {
        valuesFrom(context)
        // Propagate coroutine name from coroutine context to log context if present
        coroutineName?.let(Logger::CoroutineName)?.let(::value)
        contextSpec()
    }
}