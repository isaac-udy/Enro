package dev.enro.test

import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.interceptor.builder.navigationInterceptor


/**
 * Creates a real NavigationContainer for testing purposes.
 */
fun createTestNavigationContainer(
    key: NavigationContainer.Key,
    backstack: NavigationBackstack = emptyList(),
    parent: NavigationContainer? = null,
): NavigationContainer {
    val controller = EnroTest.getCurrentNavigationController()
    val container = NavigationContainer(
        key = key,
        controller = controller,
        backstack = backstack,
    )
    container.backstack.value.forEach {
        val currentContainer = it.metadata.get(TestNavigationContainer.MetadataKey)
        if (currentContainer != null) {
            require(currentContainer == container) {
                "TODO better error message"
            }
        }
        it.metadata.set(TestNavigationContainer.MetadataKey, container)
    }
    container.addInterceptor(navigationInterceptor {
        onTransition {
            transition.targetBackstack.forEach {
                val currentContainer = it.metadata.get(TestNavigationContainer.MetadataKey)
                if (currentContainer != null) {
                    require(currentContainer == container) {
                        "TODO better error message"
                    }
                }
                it.metadata.set(TestNavigationContainer.MetadataKey, container)
            }
            continueWithTransition()
        }
    })
    return container
}

/**
 * Sets the backstack of this container
 */
fun NavigationContainer.setBackstack(backstack: NavigationBackstack) {
    execute(NavigationOperation { backstack })
}

/**
 * Pushes a navigation key onto the backstack
 */
fun NavigationContainer.push(key: NavigationKey) {
    execute(NavigationOperation.open(key.asInstance()))
}

/**
 * Closes a specific instance from the backstack
 */
fun NavigationContainer.close(instance: NavigationKey.Instance<*>) {
    execute(NavigationOperation.close(instance))
}

/**
 * Common container keys used in tests - kept for backward compatibility
 */
object TestNavigationContainer {
    val parentContainer = NavigationContainer.Key("TestNavigationContainer.parentKey")
    val activeContainer = NavigationContainer.Key("TestNavigationContainer.activeContainer")

    object MetadataKey : NavigationKey.TransientMetadataKey<NavigationContainer?>(null)
}
