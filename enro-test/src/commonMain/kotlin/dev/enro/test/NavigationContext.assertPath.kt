@file:OptIn(ExperimentalEnroApi::class)

package dev.enro.test

import dev.enro.EnroController
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.path.getPathFromNavigationKey
import kotlin.reflect.KClass

/**
 * Asserts that the controller's path bindings resolve [path] to a [NavigationKey] of type
 * [keyType] that matches [predicate]. Returns the resolved key.
 */
public fun <T : NavigationKey> EnroController.assertPathResolvesTo(
    path: String,
    keyType: KClass<T>,
    predicate: (T) -> Boolean = { true },
): T {
    val resolved = getNavigationKeyFromPath(path)
    resolved.shouldNotBeEqualTo(null) {
        "Expected $path to resolve to ${keyType.simpleName}, but no registered path binding matched"
    }
    enroAssert(keyType.isInstance(resolved)) {
        "Expected $path to resolve to ${keyType.simpleName}, but resolved to ${resolved!!::class.simpleName} ($resolved)"
    }
    @Suppress("UNCHECKED_CAST")
    val typed = resolved as T
    typed.shouldMatchPredicate(predicate) {
        "Expected $path to resolve to a ${keyType.simpleName} matching the predicate, but got: $typed"
    }
    return typed
}

public inline fun <reified T : NavigationKey> EnroController.assertPathResolvesTo(
    path: String,
    noinline predicate: (T) -> Boolean = { true },
): T = assertPathResolvesTo(path, T::class, predicate)

public inline fun <reified T : NavigationKey> NavigationContext.assertPathResolvesTo(
    path: String,
    noinline predicate: (T) -> Boolean = { true },
): T = controller.assertPathResolvesTo(path, T::class, predicate)

/**
 * Asserts that the controller's path bindings do not resolve [path] to a [NavigationKey].
 * The assertion also passes when resolving [path] throws.
 */
public fun EnroController.assertPathDoesNotResolve(path: String) {
    val resolved = runCatching { getNavigationKeyFromPath(path) }.getOrNull()
    enroAssert(resolved == null) {
        "Expected $path to not resolve, but it resolved to $resolved"
    }
}

public fun NavigationContext.assertPathDoesNotResolve(path: String): Unit =
    controller.assertPathDoesNotResolve(path)

/**
 * Asserts that the controller's path bindings serialize [key] to exactly [expectedPath].
 * `assertPathResolvesTo` asserts the opposite direction.
 */
public fun EnroController.assertPathFor(
    key: NavigationKey,
    expectedPath: String,
) {
    val actual = getPathFromNavigationKey(key)
    actual.shouldNotBeEqualTo(null) {
        "Expected $key to serialise to $expectedPath, but no registered path binding matched"
    }
    actual.shouldBeEqualTo(expectedPath) {
        "Expected $key to serialise to $expectedPath, but produced $actual"
    }
}

public fun NavigationContext.assertPathFor(
    key: NavigationKey,
    expectedPath: String,
): Unit = controller.assertPathFor(key, expectedPath)
