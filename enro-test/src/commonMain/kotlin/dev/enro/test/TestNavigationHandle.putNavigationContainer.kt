package dev.enro.test

import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationKey

/**
 * Puts a NavigationContainer into the TestNavigationHandle with the given [key] and [backstack]. This is useful for
 * unit tests that are testing navigation behaviour. By default, TestNavigationHandles used in unit tests will have
 * a parent container and an active container (referencable through [TestNavigationContainer.parentContainer]
 * and [TestNavigationContainer.activeContainer], but if a test needs to test navigation behaviour in a container that
 * uses a specific NavigationContainer.Key, this function can be used to put a NavigationContainer into the TestNavigationHandle.
 *
 * This method can also be used to set up the state of the parent container or active container (using
 * [TestNavigationContainer.parentContainer] or [TestNavigationContainer.activeContainer] respectively), if a test needs to
 * configure the state of the parent or active container's backstack.
 */
fun TestNavigationHandle<*>.putNavigationContainer(
    key: NavigationContainer.Key,
    backstack: NavigationBackstack,
): NavigationContainer {
    val container = createTestNavigationContainer(key, backstack)
    navigationContainers[key] = container
    return container
}

/**
 * This is a shortcut for [putNavigationContainer] that allows for a more concise syntax when setting up a container
 * with a backstack, allowing the use of varargs to define the backstack (rather than providing a NavigationBackstack).
 *
 * @see putNavigationContainer
 */
fun TestNavigationHandle<*>.putNavigationContainer(
    key: NavigationContainer.Key,
    vararg instances: NavigationKey.Instance<*>,
): NavigationContainer = putNavigationContainer(key, instances.toList())
