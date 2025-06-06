@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance

class TestNavigationHandle<T : NavigationKey>(
    override val instance: NavigationKey.Instance<T>,
) : NavigationHandle<T>() {

    private val rootContext = createRootContext()
    private val parentContext = createContainerContext(
        parent = rootContext,
        container = createTestNavigationContainer(
            key = TestNavigationContainer.parentContainer,
            backstack = listOf(instance)
        )
    )

    private val context = createDestinationContext(
        parent = parentContext,
        instance = instance,
        metadata = emptyMap(),
    )

    @PublishedApi
    internal val navigationContainers = mutableMapOf(
        TestNavigationContainer.parentContainer to createTestNavigationContainer(
            key = TestNavigationContainer.parentContainer,
            backstack = listOf(instance)
        ),
    )

    val parentContainer = parentContext.container

    override val lifecycle: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override fun execute(operation: NavigationOperation) {
        require(parentContainer.backstack.any { it.id == instance.id }) {
            "TestNavigationHandle can't execute NavigationOperation, as the associated NavigationKey.Instance has been removed from it's parent backstack."
        }
        parentContainer.execute(context, operation)
    }
}

/**
 * Create a TestNavigationHandle to be used in tests.
 */
fun <T : NavigationKey> createTestNavigationHandle(
    key: T,
): TestNavigationHandle<T> {
    return createTestNavigationHandle(key.asInstance())
}

/**
 * Create a TestNavigationHandle to be used in tests with a NavigationKey.WithMetadata.
 */
fun <T : NavigationKey> createTestNavigationHandle(
    key: NavigationKey.WithMetadata<T>,
): TestNavigationHandle<T> {
    return createTestNavigationHandle(key.asInstance())
}

/**
 * Create a TestNavigationHandle to be used in tests with a NavigationKey.Instance.
 */
fun <T : NavigationKey> createTestNavigationHandle(
    instance: NavigationKey.Instance<T>,
): TestNavigationHandle<T> {
    return TestNavigationHandle(instance)
}
