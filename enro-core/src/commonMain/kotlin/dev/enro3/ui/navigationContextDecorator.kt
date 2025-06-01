package dev.enro3.ui

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro3.NavigationBackstack
import dev.enro3.NavigationContext
import dev.enro3.NavigationKey
import dev.enro3.handle.NavigationHandleHolder

internal fun <T: NavigationKey> navigationContextDecorator(
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
        val localViewModelStoreOwner = LocalViewModelStoreOwner.current

        val owner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        } as ViewModelStoreOwner

        val navigationHandleHolder = viewModel<NavigationHandleHolder<T>>(
            viewModelStoreOwner = owner,
        ) {
            NavigationHandleHolder(instance = destination.instance)
        }

        val context = NavigationContext(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = owner,
            defaultViewModelProviderFactory = object : HasDefaultViewModelProviderFactory {
                override val defaultViewModelCreationExtras: CreationExtras get() {
                    return MutableCreationExtras().apply {
                        set(VIEW_MODEL_STORE_OWNER_KEY, owner)
                    }
                }
                override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                    get() {
                        val parentDefault = localViewModelStoreOwner as? HasDefaultViewModelProviderFactory
                        requireNotNull(parentDefault) {
                            "No default ViewModelProvider.Factory found for ViewModelStoreOwner: $localViewModelStoreOwner"
                        }
                        return parentDefault.defaultViewModelProviderFactory
                    }
            },
            destination = destination,
            parentContainer = container,
            childContainers = emptyList()
        )
        navigationHandleHolder.bindContext(context)
        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalLifecycleOwner provides context,
            LocalViewModelStoreOwner provides context,
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
