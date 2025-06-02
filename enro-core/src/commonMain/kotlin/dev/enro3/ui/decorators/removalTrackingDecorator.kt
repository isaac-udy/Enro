package dev.enro3.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro3.NavigationKey
import dev.enro3.platform.EnroLog
import dev.enro3.ui.LocalNavigationContainer

/**
 * Returns a [NavigationDestinationDecorator] that tracks when destinations are removed
 * from the backstack and invokes the onRemove callbacks of all decorators.
 *
 * This decorator should be applied last to ensure it can track all other decorators'
 * onRemove callbacks and invoke them when destinations are removed.
 *
 * @param backstack The current navigation backstack
 * @param decorators The list of decorators whose onRemove callbacks should be tracked
 */
@Composable
public fun rememberRemovalTrackingDecorator(
    decorators: List<NavigationDestinationDecorator<*>>,
): NavigationDestinationDecorator<NavigationKey> = remember(decorators) {
    removalTrackingDecorator(decorators)
}

// TODO: I think it should be possible to use lifecycle state tracking to perform the same functionality that this
//  decorator provides. Possibly worth looking into, as that would simplify things quite a bit
/**
 * Creates a decorator that tracks destination removal and invokes onRemove callbacks.
 *
 * This implementation is inspired by navigation3's approach, using DisposableEffect
 * to detect when destinations are removed from composition and the backstack.
 */
internal fun removalTrackingDecorator(
    decorators: List<NavigationDestinationDecorator<*>>,
): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator { destination ->
        val container = LocalNavigationContainer.current
        val removalInfo = LocalRemovalTrackingInfo.current
        val instance = destination.instance
        val id = instance.id

        // Store onRemove callbacks for all decorators
        val popCallbacks = remember(instance) {
            mutableSetOf<(NavigationKey.Instance<NavigationKey>) -> Unit>()
        }

        // Collect onRemove callbacks from all decorators
        @Suppress("UNCHECKED_CAST")
        decorators.distinct().forEach { decorator ->
            popCallbacks.add(decorator.onRemove as (NavigationKey.Instance<NavigationKey>) -> Unit)
        }

        // Track this destination's lifecycle
        DisposableEffect(instance) {
            // Mark as in composition
            removalInfo.idsInComposition.add(id)

            // Update reference count
            removalInfo.keyRefCounts[instance] = (removalInfo.keyRefCounts[instance] ?: 0) + 1

            onDispose {
                // Remove from composition tracking
                val wasInComposition = removalInfo.idsInComposition.remove(id)

                // Check if this destination is still in the backstack
                val stillInBackstack = container.backstack.value.any { it.id == id }

                // Update reference count
                val currentCount = removalInfo.keyRefCounts[instance] ?: 1
                if (currentCount > 1) {
                    removalInfo.keyRefCounts[instance] = currentCount - 1
                } else {
                    removalInfo.keyRefCounts.remove(instance)
                }

                // If removed from composition and not in backstack, call onRemove
                EnroLog.error(
                    "${instance.id} $wasInComposition && ${!stillInBackstack} && ${removalInfo.keyRefCounts[instance]}"
                )
                if (wasInComposition && !stillInBackstack && removalInfo.keyRefCounts[instance] == null) {
                    // Call onRemove in reverse order (similar to navigation3)
                    EnroLog.error("Calling onRemove for $instance ${popCallbacks.size}")
                    popCallbacks.toList().reversed().forEach { callback ->
                        @Suppress("UNCHECKED_CAST")
                        callback(instance as NavigationKey.Instance<NavigationKey>)
                    }
                }
            }
        }

        destination.content()
    }
}

/**
 * Provides removal tracking information to the decorated destinations.
 * This should be called at the NavigationDisplay level to wrap all destinations.
 */
@Composable
public fun ProvideRemovalTrackingInfo(
    content: @Composable () -> Unit,
) {
    val removalInfo = remember { RemovalTrackingInfo() }
    CompositionLocalProvider(LocalRemovalTrackingInfo provides removalInfo) {
        content()
    }
}

/**
 * Internal class that tracks the state needed for removal detection.
 */
private class RemovalTrackingInfo {
    /** Set of destination IDs currently in composition */
    val idsInComposition: MutableSet<String> = mutableSetOf()

    /** Reference counts for each destination instance (handles duplicates) */
    val keyRefCounts: MutableMap<NavigationKey.Instance<*>, Int> = mutableMapOf()
}

/**
 * CompositionLocal that provides access to removal tracking information.
 */
private val LocalRemovalTrackingInfo = staticCompositionLocalOf<RemovalTrackingInfo> {
    error(
        "LocalRemovalTrackingInfo not provided. Ensure ProvideRemovalTrackingInfo " +
                "is called before using removalTrackingDecorator."
    )
}
