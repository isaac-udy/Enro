package dev.enro.test

import dev.enro.NavigationContainer
import dev.enro.NavigationKey

/**
 * Asserts that the NavigationContainer's backstack contains at least one NavigationKey.Instance that matches the
 * provided predicate.
 *
 * @return The first NavigationKey.Instance that matches the predicate
 */
inline fun <reified T : NavigationKey> NavigationContainer.assertContains(
    predicate: (NavigationKey.Instance<T>) -> Boolean = { true },
): NavigationKey.Instance<T> {
    backstack.value
        .firstOrNull {
            @Suppress("UNCHECKED_CAST")
            it.key is T && predicate(it as NavigationKey.Instance<T>)
        }
        .shouldNotBeEqualTo(null) {
            "NavigationContainer's backstack does not contain expected NavigationKey.Instance.\n\tBackstack: ${backstack.value}"
        }
        .let {
            @Suppress("UNCHECKED_CAST")
            return it as NavigationKey.Instance<T>
        }
}

/**
 * Asserts that the NavigationContainer's backstack contains at least one NavigationKey.Instance that is equal
 * to the provided NavigationKey.Instance [instance]
 */
fun NavigationContainer.assertContains(
    predicate: (NavigationKey.Instance<NavigationKey>) -> Boolean = { true },
): NavigationKey.Instance<NavigationKey> {
    return assertContains<NavigationKey> { predicate(it) }
}

/**
 * Asserts that the NavigationContainer's backstack contains at least one NavigationKey.Instance that is equal
 * to the provided NavigationKey.Instance [instance]
 */
inline fun <reified T : NavigationKey> NavigationContainer.assertContains(
    instance: NavigationKey.Instance<T>,
): NavigationKey.Instance<T> {
    return assertContains<T> { it == instance }
}


/**
 * Asserts that the NavigationContainer's backstack contains at least one NavigationKey.Instance that has a
 * NavigationKey that is equal to the provided NavigationKey [key]
 */
inline fun <reified T : NavigationKey> NavigationContainer.assertContains(key: T): NavigationKey.Instance<T> {
    return assertContains<T> { it.key == key }
}

/**
 * Asserts that the NavigationContainer's backstack does not contain a NavigationKey.Instance that matches the provided
 * predicate
 */
inline fun <reified T : NavigationKey> NavigationContainer.assertDoesNotContain(
    predicate: (NavigationKey.Instance<T>) -> Boolean,
) {
    backstack.value
        .firstOrNull {
            @Suppress("UNCHECKED_CAST")
            it.key is T && predicate(it as NavigationKey.Instance<T>)
        }
        .shouldBeEqualTo(
            null,
        ) {
            "NavigationContainer's backstack should not contain NavigationKey.Instance matching predicate.\n\tBackstack: ${backstack.value}"
        }
}

fun NavigationContainer.assertDoesNotContain(
    predicate: (NavigationKey.Instance<NavigationKey>) -> Boolean,
) {
    assertDoesNotContain<NavigationKey>(predicate)
}

/**
 * Asserts that the NavigationContainer's backstack does not contain a NavigationKey.Instance that is equal to
 * the provided NavigationKey.Instance [instance]
 */
inline fun <reified T : NavigationKey> NavigationContainer.assertDoesNotContain(
    instance: NavigationKey.Instance<T>,
) {
    backstack.value.firstOrNull { it == instance }
        .shouldNotBeEqualTo(
            instance,
        ) {
            "NavigationContainer's backstack should not contain NavigationKey.Instance.\n\tNavigationKey.Instance: $expected\n\tBackstack: ${backstack.value}"
        }
}

/**
 * Asserts that the NavigationContainer's backstack does not contain an instance that has a NavigationKey that is
 * equal to the provided NavigationKey [key]
 */
fun NavigationContainer.assertDoesNotContain(
    key: NavigationKey,
) {
    val backstackAsNavigationKeys = backstack.value.map { it.key }
    backstackAsNavigationKeys.firstOrNull { it == key }
        .shouldNotBeEqualTo(
            key,
        ) {
            "NavigationContainer's backstack should not contain NavigationKey.\n\tNavigationKey: $expected\n\tBackstack: $backstackAsNavigationKeys"
        }
}
