package dev.enro3

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

public val LocalNavigationContext: ProvidableCompositionLocal<NavigationContext<out NavigationKey>> = staticCompositionLocalOf {
    error("No LocalNavigationContext")
}

public class NavigationContext<T : NavigationKey>(
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,

    public val destination: NavigationDestination<T>,
    public val parentContainer: NavigationContainer,

    // TODO need some kind of ContainerManager type thing for child containers
    public val childContainers: List<NavigationContainer>,
) : LifecycleOwner by lifecycleOwner, ViewModelStoreOwner by viewModelStoreOwner

public fun <T: NavigationKey> localNavigationContextDecorator(
    backstack: NavigationBackstack,
    isSettled: Boolean,
): NavigationDestinationDecorator<T> {
    return navigationDestinationDecorator { destination ->
        val container = LocalNavigationContainer.current
        val isInBackstack = backstack.contains(destination.instance)

        val lifecycleOwner = rememberNavigationLifecycleOwner(
            maxLifecycle = when {
                isInBackstack && isSettled -> Lifecycle.State.RESUMED
                isInBackstack && !isSettled -> Lifecycle.State.STARTED
                else /* !isInBackStack */ -> Lifecycle.State.CREATED
            },
            parentLifecycleOwner = LocalLifecycleOwner.current
        )
        val context = NavigationContext(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            },
            destination = destination,
            parentContainer = container,
            childContainers = emptyList()
        )
        val navigationHandleHolder = viewModel<NavigationHandleHolder<T>>(
            viewModelStoreOwner = context,
        ) {
            NavigationHandleHolder(instance = destination.instance)
        }
        navigationHandleHolder.bindContext(context)
        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalLifecycleOwner provides context,
            LocalNavigationHandle provides navigationHandleHolder.navigationHandle,
        ) {
            destination.content()
        }
    }
}

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

@Composable
public fun <T: NavigationKey> navigationHandle(): NavigationHandle<T> {
    val holder = viewModel<NavigationHandleHolder<T>>(
        viewModelStoreOwner = LocalNavigationContext.current,
    ) {
        error("TODO better error")
    }
    return holder.navigationHandle
}

