@file:OptIn(AdvancedEnroApi::class)

package dev.enro.test

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.asInstance
import dev.enro.test.fixtures.NavigationContainerFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.destinations.SyntheticOutcome
import dev.enro.ui.destinations.peekSyntheticOutcome

/**
 * Runs the synthetic destination bound to [key] on the controller installed by
 * [runEnroTest], and returns the [SyntheticOutcome].
 *
 * A `NavigationModule` on the controller must register the synthetic destination:
 * `runEnroTest { installNavigationModule(myModule); testSyntheticDestination(MyKey) }`.
 * To test a synthetic destination without a `NavigationModule`, call the
 * `testSyntheticDestination(key, provider, ...)` overload.
 *
 * Throws [IllegalStateException] when [key] is not bound to a synthetic destination on
 * the controller.
 */
public fun <K : NavigationKey> testSyntheticDestination(
    key: K,
    fromContext: NavigationContext = NavigationContextFixtures.createRootContext(),
): SyntheticOutcome {
    val controller = EnroTest.getCurrentNavigationController()
    return controller.peekSyntheticOutcome(key, fromContext)
        ?: error(
            "Key ${key::class.simpleName} is not bound to a synthetic destination on the current " +
                "EnroController. Install a NavigationModule that registers the synthetic, or pass " +
                "the NavigationDestinationProvider directly to the testSyntheticDestination(key, provider) overload."
        )
}

/**
 * Runs the synthetic destination [provider] for [key] and returns the [SyntheticOutcome].
 * [provider] is the value of a `syntheticDestination<K> { ... }` declaration; no
 * `NavigationModule` is required.
 *
 * Throws [IllegalStateException] when [provider] is not a synthetic destination.
 */
public fun <K : NavigationKey> testSyntheticDestination(
    key: K,
    provider: NavigationDestinationProvider<K>,
    fromContext: NavigationContext = NavigationContextFixtures.createRootContext(),
): SyntheticOutcome {
    val instance = key.asInstance()
    return provider.peekSyntheticOutcome(fromContext, instance)
        ?: error(
            "The provided NavigationDestinationProvider for ${key::class.simpleName} is not a " +
                "synthetic destination — it doesn't carry the SyntheticDestinationKey metadata. " +
                "Was the provider built with `syntheticDestination<K> { ... }`?"
        )
}

/**
 * Runs the side effect with a new root context and a new container from the test fixtures.
 */
public fun SyntheticOutcome.SideEffect.runWith() {
    val context = NavigationContextFixtures.createRootContext()
    val containerState = NavigationContainerFixtures.create(parentContext = context)
    runWith(context = context, container = containerState.container)
}

/**
 * Asserts that the outcome is [SyntheticOutcome.Open] with a key of type [Expected] that
 * matches [keyPredicate]. Returns the opened key.
 */
public inline fun <reified Expected : NavigationKey> SyntheticOutcome.assertOpens(
    keyPredicate: (Expected) -> Boolean = { true },
): Expected {
    val open = this as? SyntheticOutcome.Open
        ?: enroAssertionError("Expected synthetic to open ${Expected::class.simpleName}, but outcome was $this")
    val key = open.key as? Expected
        ?: enroAssertionError("Expected synthetic to open ${Expected::class.simpleName}, but opened ${open.key::class.simpleName}")
    if (!keyPredicate(key)) {
        enroAssertionError("Synthetic opened ${Expected::class.simpleName}, but the key didn't match the predicate; got: $key")
    }
    return key
}

/**
 * Asserts that the outcome is [SyntheticOutcome.CompleteFrom] with a key of type [Expected]
 * that matches [keyPredicate]. Returns the key.
 */
public inline fun <reified Expected : NavigationKey> SyntheticOutcome.assertCompletesFrom(
    keyPredicate: (Expected) -> Boolean = { true },
): Expected {
    val completeFrom = this as? SyntheticOutcome.CompleteFrom
        ?: enroAssertionError("Expected synthetic to completeFrom ${Expected::class.simpleName}, but outcome was $this")
    val key = completeFrom.key as? Expected
        ?: enroAssertionError("Expected synthetic to completeFrom ${Expected::class.simpleName}, but completed from ${completeFrom.key::class.simpleName}")
    if (!keyPredicate(key)) {
        enroAssertionError("Synthetic completed from ${Expected::class.simpleName}, but the key didn't match the predicate; got: $key")
    }
    return key
}

/**
 * Asserts that the outcome is [SyntheticOutcome.Close]. When [silent] is not null, also
 * asserts that the `silent` value of the close equals [silent].
 */
public fun SyntheticOutcome.assertCloses(silent: Boolean? = null) {
    val close = this as? SyntheticOutcome.Close
        ?: enroAssertionError("Expected synthetic to close, but outcome was $this")
    if (silent != null && close.silent != silent) {
        enroAssertionError("Expected close with silent=$silent, but got silent=${close.silent}")
    }
}

/**
 * Asserts that the outcome is [SyntheticOutcome.Complete] with a result equal to
 * [expectedResult]. Pass `null` for a synthetic destination that calls `complete()`
 * without a result.
 */
public fun SyntheticOutcome.assertCompletes(expectedResult: Any?) {
    val complete = this as? SyntheticOutcome.Complete
        ?: enroAssertionError("Expected synthetic to complete, but outcome was $this")
    if (complete.result != expectedResult) {
        enroAssertionError("Expected complete with result $expectedResult, but got ${complete.result}")
    }
}

/**
 * Asserts that the outcome is [SyntheticOutcome.SideEffect] and returns the side effect.
 * [SyntheticOutcome.SideEffect.runWith] executes the side effect.
 */
public fun SyntheticOutcome.assertSideEffect(): SyntheticOutcome.SideEffect {
    return this as? SyntheticOutcome.SideEffect
        ?: enroAssertionError("Expected synthetic to produce a side effect, but outcome was $this")
}
