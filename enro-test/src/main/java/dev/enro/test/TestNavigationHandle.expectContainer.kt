package dev.enro.test

import dev.enro.core.NavigationContainerKey
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.onActiveContainer
import dev.enro.core.onContainer
import dev.enro.core.onParentContainer

/**
 * Asserts that the NavigationHandle has a parent container, and then returns the NavigationContainerContext associated
 * with that container, which can be used for further assertions.
 */
fun TestNavigationHandle<*>.expectParentContainer(): NavigationContainerContext {
    var container: NavigationContainerContext? = null
    onParentContainer { container = this@onParentContainer }
    container.shouldNotBeEqualTo(null) {
        "NavigationHandle does not have a parent container"
    }
    return requireNotNull(container)
}

/**
 * Asserts that the NavigationHandle has an active container, and then returns the NavigationContainerContext associated
 * with that container, which can be used for further assertions.
 */
fun TestNavigationHandle<*>.expectActiveContainer(): NavigationContainerContext {
    var container: NavigationContainerContext? = null
    onActiveContainer { container = this@onActiveContainer }
    container.shouldNotBeEqualTo(null) {
        "NavigationHandle does not have an active container"
    }
    return requireNotNull(container)
}

/**
 * Asserts that the NavigationHandle has a container with the provided NavigationContainerKey [key], and then returns
 * the NavigationContainerContext associated with that container, which can be used for further assertions.
 */
fun TestNavigationHandle<*>.expectContainer(key: NavigationContainerKey): NavigationContainerContext {
    var container: NavigationContainerContext? = null
    onContainer(key) { container = this@onContainer }
    container.shouldNotBeEqualTo(null) {
        "NavigationHandle does not have a container with key $key"
    }
    return requireNotNull(container)
}