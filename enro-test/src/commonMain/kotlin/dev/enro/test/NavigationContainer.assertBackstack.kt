package dev.enro.test

import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.ui.NavigationContainerState
import kotlin.reflect.KClass

/**
 * Asserts the container's backstack has exactly [expected] entries.
 */
public fun NavigationContainer.assertBackstackSize(expected: Int) {
    enroAssert(backstack.size == expected) {
        "Expected backstack to have $expected entries, but had ${backstack.size}: ${backstack.map { it.key }}"
    }
}

public fun NavigationContainerState.assertBackstackSize(expected: Int): Unit =
    container.assertBackstackSize(expected)

/**
 * Asserts the container's backstack contains exactly the given [keys] in order.
 * Matches on key equality, not instance identity.
 */
public fun NavigationContainer.assertBackstackKeys(vararg keys: NavigationKey) {
    val actual = backstack.map { it.key }
    val expected = keys.toList()
    enroAssert(actual == expected) {
        "Expected backstack keys to be $expected, but was $actual"
    }
}

public fun NavigationContainerState.assertBackstackKeys(vararg keys: NavigationKey): Unit =
    container.assertBackstackKeys(*keys)

/**
 * Asserts the backstack contains at least one entry of [keyType], optionally
 * matching [predicate]. Returns the first matching instance for further
 * assertions.
 */
public fun <T : NavigationKey> NavigationContainer.assertBackstackContains(
    keyType: KClass<T>,
    predicate: (T) -> Boolean = { true },
): NavigationKey.Instance<T> {
    val matching = backstack
        .filter { keyType.isInstance(it.key) }
        .filter {
            @Suppress("UNCHECKED_CAST")
            predicate(it.key as T)
        }
    enroAssert(matching.isNotEmpty()) {
        "Expected backstack to contain a ${keyType.simpleName} matching the predicate, " +
            "but backstack was: ${backstack.map { it.key }}"
    }
    @Suppress("UNCHECKED_CAST")
    return matching.first() as NavigationKey.Instance<T>
}

public inline fun <reified T : NavigationKey> NavigationContainer.assertBackstackContains(
    noinline predicate: (T) -> Boolean = { true },
): NavigationKey.Instance<T> = assertBackstackContains(T::class, predicate)

public inline fun <reified T : NavigationKey> NavigationContainerState.assertBackstackContains(
    noinline predicate: (T) -> Boolean = { true },
): NavigationKey.Instance<T> = container.assertBackstackContains(T::class, predicate)

/**
 * Asserts the backstack does NOT contain any entry of [keyType].
 */
public fun NavigationContainer.assertBackstackDoesNotContain(keyType: KClass<out NavigationKey>) {
    val matching = backstack.filter { keyType.isInstance(it.key) }
    enroAssert(matching.isEmpty()) {
        "Expected backstack to not contain any ${keyType.simpleName}, " +
            "but found ${matching.size}: ${matching.map { it.key }}"
    }
}

public inline fun <reified T : NavigationKey> NavigationContainer.assertBackstackDoesNotContain(): Unit =
    assertBackstackDoesNotContain(T::class)

public inline fun <reified T : NavigationKey> NavigationContainerState.assertBackstackDoesNotContain(): Unit =
    container.assertBackstackDoesNotContain(T::class)

/**
 * Asserts the backstack is empty.
 */
public fun NavigationContainer.assertBackstackEmpty() {
    enroAssert(backstack.isEmpty()) {
        "Expected backstack to be empty, but had ${backstack.size} entries: ${backstack.map { it.key }}"
    }
}

public fun NavigationContainerState.assertBackstackEmpty(): Unit = container.assertBackstackEmpty()
