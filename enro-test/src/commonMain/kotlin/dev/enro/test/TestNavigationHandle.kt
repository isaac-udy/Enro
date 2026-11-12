@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance

/**
 * A [NavigationHandle] that records each executed [NavigationOperation] instead of
 * executing the operation on a navigation container. Side effect operations are not
 * recorded.
 *
 * The assertion functions ([assertOpened], [assertClosed], [assertCompleted] and others)
 * read the recorded operations. [clearOperationHistory] removes the recorded operations.
 *
 * After a Close or Complete for the handle's own [instance] is recorded, [execute] throws
 * until [clearOperationHistory] is called.
 */
public class TestNavigationHandle<out T : NavigationKey>(
    override val instance: NavigationKey.Instance<T>,
    override val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : NavigationHandle<T>() {
    @PublishedApi
    internal val operations: MutableList<NavigationOperation.RootOperation> = mutableListOf()

    override val lifecycle: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    public fun clearOperationHistory() {
        operations.clear()
    }

    override fun execute(operation: NavigationOperation) {
        val lastOperation = operations.lastOrNull()
        when (lastOperation) {
            is NavigationOperation.Close<*> -> {
                require(lastOperation.instance.id != instance.id) {
                    "Cannot execute NavigationOperation on TestNavigationHandle that is closed. If you want to continue using the TestNavigationHandle after it is closed, you need to call clearOperationHistory."
                }
            }
            is NavigationOperation.Complete<*> -> {
                require(lastOperation.instance.id != instance.id) {
                    "Cannot execute NavigationOperation on TestNavigationHandle that is completed. If you want to continue using the TestNavigationHandle after it is completed, you need to call clearOperationHistory."
                }
            }
            else -> {
            }
        }
        when (operation) {
            is NavigationOperation.AggregateOperation -> operations.addAll(operation.operations)
            is NavigationOperation.Close<*> -> operations.add(operation)
            is NavigationOperation.Complete<*> -> operations.add(operation)
            is NavigationOperation.Open<*> -> operations.add(operation)
            is NavigationOperation.SideEffect -> {}
        }
    }
}

public fun <T : NavigationKey> createTestNavigationHandle(
    key: T,
): TestNavigationHandle<T> {
    return createTestNavigationHandle(key.asInstance())
}

public fun <T : NavigationKey> createTestNavigationHandle(
    key: NavigationKey.WithMetadata<T>,
): TestNavigationHandle<T> {
    return createTestNavigationHandle(key.asInstance())
}

public fun <T : NavigationKey> createTestNavigationHandle(
    instance: NavigationKey.Instance<T>,
): TestNavigationHandle<T> {
    return TestNavigationHandle(instance)
}
