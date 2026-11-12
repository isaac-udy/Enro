package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.enro.NavigationBackstack
import dev.enro.NavigationKey

/**
 * Returns a decorator that provides each destination with a [LifecycleOwner] through
 * [LocalLifecycleOwner]. The destination's lifecycle state is the lower of the parent
 * lifecycle's state and:
 * - **RESUMED**: the destination is in [backstack] and [isSettled] is `true`
 * - **STARTED**: the destination is in [backstack] and [isSettled] is `false`
 * - **CREATED**: the destination is not in [backstack], for example while animating out
 *
 * @param isSettled `false` while a navigation animation is running
 */
@Composable
public fun rememberLifecycleDecorator(
    backstack: NavigationBackstack,
    isSettled: Boolean,
): NavigationDestinationDecorator<NavigationKey> = remember(backstack, isSettled) {
    navigationLifecycleDecorator(backstack, isSettled)
}

internal fun navigationLifecycleDecorator(
    backstack: NavigationBackstack,
    isSettled: Boolean,
): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator { destination ->
        val isInBackstack = backstack.contains(destination.instance)

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
 * Returns a [LifecycleOwner] whose state is the lower of [maxLifecycle] and the state of
 * [parentLifecycleOwner].
 */
@Composable
private fun rememberNavigationLifecycleOwner(
    maxLifecycle: Lifecycle.State,
    parentLifecycleOwner: LifecycleOwner,
) : LifecycleOwner {
    val childLifecycleOwner = remember(parentLifecycleOwner) { ChildLifecycleOwner() }
    // maxLifecycle is set in a SideEffect, which runs in the composition's apply phase, and not
    // in a dispatched effect: the child must not receive a parent ON_DESTROY while the child's
    // maxLifecycle is still INITIALIZED.
    SideEffect {
        childLifecycleOwner.maxLifecycle = maxLifecycle
    }
    DisposableEffect(childLifecycleOwner, parentLifecycleOwner) {
        childLifecycleOwner.maxLifecycle = maxLifecycle
        val observer = LifecycleEventObserver { _, event ->
            childLifecycleOwner.handleLifecycleEvent(event)
        }

        parentLifecycleOwner.lifecycle.addObserver(observer)

        onDispose { parentLifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return childLifecycleOwner
}

internal class ChildLifecycleOwner : LifecycleOwner {
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
        val targetState = when {
            parentLifecycleState.ordinal < maxLifecycle.ordinal -> parentLifecycleState
            else -> maxLifecycle
        }
        // LifecycleRegistry cannot move from INITIALIZED straight to DESTROYED.
        if (targetState == Lifecycle.State.DESTROYED && lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        lifecycleRegistry.currentState = targetState
    }
}
