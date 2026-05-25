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
 * Executes the synthetic destination bound to [key] on the currently-installed
 * navigation controller and returns the [SyntheticOutcome] the block decided on.
 *
 * Use this when the synthetic is registered through a `NavigationModule` on the
 * controller — typical setup is `runEnroTest { MyComponent.installNavigationController(this); testSyntheticDestination(MyKey) }`.
 *
 * For unit-testing a synthetic's logic without installing a component, pass the
 * provider directly: see the `testSyntheticDestination(key, provider, ...)` overload.
 *
 * Throws [IllegalStateException] if [key] isn't bound to a synthetic destination
 * on the current controller.
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
 * Executes the synthetic destination bound to [provider] for [key] and returns
 * the [SyntheticOutcome] the block decided on.
 *
 * Use this when you have a `val synthetic = syntheticDestination<K> { ... }` you
 * want to unit-test without installing a controller component — pass the provider
 * value directly.
 *
 * Throws [IllegalStateException] if [provider] isn't a synthetic destination.
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
 * Executes the side-effect block with default fixture context and container.
 * Equivalent to calling [SyntheticOutcome.SideEffect.runWith] with freshly-built
 * test fixtures — convenient when the side effect doesn't care about the
 * specific context/container it's given.
 */
public fun SyntheticOutcome.SideEffect.runWith() {
    val context = NavigationContextFixtures.createRootContext()
    val containerState = NavigationContainerFixtures.create(parentContext = context)
    runWith(context = context, container = containerState.container)
}

/**
 * Asserts the outcome is [SyntheticOutcome.Open] of [Expected], optionally
 * matching [keyPredicate]. Returns the opened key for further assertions.
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
 * Asserts the outcome is [SyntheticOutcome.CompleteFrom] of [Expected], optionally
 * matching [keyPredicate]. Returns the forwarded key for further assertions.
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
 * Asserts the outcome is a close, optionally matching the silent flag.
 */
public fun SyntheticOutcome.assertCloses(silent: Boolean? = null) {
    val close = this as? SyntheticOutcome.Close
        ?: enroAssertionError("Expected synthetic to close, but outcome was $this")
    if (silent != null && close.silent != silent) {
        enroAssertionError("Expected close with silent=$silent, but got silent=${close.silent}")
    }
}

/**
 * Asserts the outcome is a complete with the expected payload. Pass `null` for
 * non-result synthetics that call `complete()`.
 */
public fun SyntheticOutcome.assertCompletes(expectedResult: Any?) {
    val complete = this as? SyntheticOutcome.Complete
        ?: enroAssertionError("Expected synthetic to complete, but outcome was $this")
    if (complete.result != expectedResult) {
        enroAssertionError("Expected complete with result $expectedResult, but got ${complete.result}")
    }
}

/**
 * Asserts the outcome is a side effect and returns it for further assertion
 * (e.g. calling [SyntheticOutcome.SideEffect.runWith] to execute it).
 */
public fun SyntheticOutcome.assertSideEffect(): SyntheticOutcome.SideEffect {
    return this as? SyntheticOutcome.SideEffect
        ?: enroAssertionError("Expected synthetic to produce a side effect, but outcome was $this")
}
