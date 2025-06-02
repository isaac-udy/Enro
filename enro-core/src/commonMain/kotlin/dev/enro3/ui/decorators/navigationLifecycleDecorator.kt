package dev.enro3.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.enro3.NavigationBackstack
import dev.enro3.NavigationKey

/**
 * Returns a [NavigationDestinationDecorator] that manages the lifecycle of navigation destinations
 * based on their position in the backstack and the current navigation state.
 *
 * The lifecycle states are determined as follows:
 * - **RESUMED**: The destination is in the backstack and navigation has settled (no animations)
 * - **STARTED**: The destination is in the backstack but navigation is transitioning
 * - **CREATED**: The destination is not in the backstack (e.g., being animated out)
 *
 * @param backstack The current navigation backstack
 * @param isSettled Whether the navigation state has settled (no animations in progress)
 */
@Composable
public fun rememberLifecycleDecorator(
    backstack: NavigationBackstack,
    isSettled: Boolean,
): NavigationDestinationDecorator<NavigationKey> = remember(backstack, isSettled) {
    navigationLifecycleDecorator(backstack, isSettled)
}

/**
 * Creates a [NavigationDestinationDecorator] that manages destination lifecycle.
 *
 * This decorator provides each destination with its own [androidx.lifecycle.LifecycleOwner]
 * that reflects the destination's visibility state within the navigation system.
 *
 * @param backstack The current navigation backstack
 * @param isSettled Whether the navigation state has settled
 */
internal fun navigationLifecycleDecorator(
    backstack: NavigationBackstack,
    isSettled: Boolean,
): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator { destination ->
        val isInBackstack = backstack.contains(destination.instance)

        // Determine the appropriate lifecycle state based on destination visibility
        val maxLifecycle = when {
            isInBackstack && isSettled -> Lifecycle.State.RESUMED
            isInBackstack && !isSettled -> Lifecycle.State.STARTED
            else /* !isInBackStack */ -> Lifecycle.State.CREATED
        }

        val parentLifecycleOwner = LocalLifecycleOwner.current
        val lifecycleOwner = rememberNavigationLifecycleOwner(
            maxLifecycle = maxLifecycle,
            parentLifecycleOwner = parentLifecycleOwner
        )

        CompositionLocalProvider(
            LocalLifecycleOwner provides lifecycleOwner
        ) {
            destination.Content()
        }
    }
}

/**
 * Creates and remembers a [LifecycleOwner] that follows the parent lifecycle but is capped
 * at the specified [maxLifecycle] state.
 *
 * This is used internally by [navigationLifecycleDecorator] to manage the lifecycle of navigation
 * destinations based on their visibility and animation state.
 *
 * @param maxLifecycle The maximum lifecycle state this owner can reach
 * @param parentLifecycleOwner The parent lifecycle to follow
 * @return A lifecycle owner that is capped at the specified max state
 */
@Composable
private fun rememberNavigationLifecycleOwner(
    maxLifecycle: Lifecycle.State,
    parentLifecycleOwner: LifecycleOwner,
) : LifecycleOwner {
    val childLifecycleOwner = remember(parentLifecycleOwner) { ChildLifecycleOwner() }
    // Pass LifecycleEvents from the parent down to the child
    DisposableEffect(childLifecycleOwner, parentLifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            childLifecycleOwner.handleLifecycleEvent(event)
        }

        parentLifecycleOwner.lifecycle.addObserver(observer)

        onDispose { parentLifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // Ensure that the child lifecycle is capped at the maxLifecycle
    LaunchedEffect(childLifecycleOwner, maxLifecycle) {
        childLifecycleOwner.maxLifecycle = maxLifecycle
    }
    return childLifecycleOwner
}

/**
 * Internal implementation of a child lifecycle owner that follows a parent lifecycle
 * but can be capped at a maximum state.
 */
private class ChildLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    var maxLifecycle: Lifecycle.State = Lifecycle.State.INITIALIZED
        set(maxState) {
            field = maxState
            updateState()
        }

    private var parentLifecycleState: Lifecycle.State = Lifecycle.State.CREATED

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        parentLifecycleState = event.targetState
        updateState()
    }

    fun updateState() {
        if (parentLifecycleState.ordinal < maxLifecycle.ordinal) {
            lifecycleRegistry.currentState = parentLifecycleState
        } else {
            lifecycleRegistry.currentState = maxLifecycle
        }
    }
}
