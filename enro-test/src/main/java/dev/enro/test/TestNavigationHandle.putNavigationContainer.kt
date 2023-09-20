package dev.enro.test

import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.toBackstack

fun TestNavigationHandle<*>.putNavigationContainer(
    key: NavigationContainerKey,
    backstack: NavigationBackstack,
) : TestNavigationContainer {
    if(navigationHandle !is FakeNavigationHandle) {
        throw IllegalStateException("Cannot putNavigationContainer: TestNavigationHandle operating in a real environment")
    }
    val container = createTestNavigationContainer(key, backstack)
    navigationHandle.navigationContainers[key] = container
    return container
}

fun TestNavigationHandle<*>.putNavigationContainer(
    key: NavigationContainerKey,
    vararg instructions: NavigationInstruction.Open<*>,
) : TestNavigationContainer = putNavigationContainer(key, instructions.toList().toBackstack())