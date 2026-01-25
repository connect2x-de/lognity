package de.connect2x.lognity.test

import de.connect2x.lognity.api.context.Context
import kotlinx.coroutines.test.TestScope
import org.jetbrains.annotations.TestOnly

/**
 * A [Context.Element] that stores a [TestScope].
 *
 * @property scope The [TestScope] to be stored in the context.
 */
@TestOnly
data class TestScopeElement(val scope: TestScope) : Context.Element {
    /**
     * The key for [TestScopeElement].
     */
    companion object Key : Context.Key<TestScopeElement>

    override val key: Context.Key<*> = Key
}