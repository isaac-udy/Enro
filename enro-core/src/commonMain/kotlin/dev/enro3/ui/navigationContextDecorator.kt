package dev.enro3.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
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
        requireNotNull(localViewModelStoreOwner)
        require(localViewModelStoreOwner is HasDefaultViewModelProviderFactory)

        val navigationHandleHolder = viewModel<NavigationHandleHolder<T>>(
            viewModelStoreOwner = localViewModelStoreOwner,
        ) {
            NavigationHandleHolder(instance = destination.instance)
        }

        val context = NavigationContext(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = localViewModelStoreOwner,
            defaultViewModelProviderFactory = localViewModelStoreOwner,
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
            destination.Content()
        }
    }
}

private class NavigationContextOwners(
    lifecycleOwner: LifecycleOwner,
) : SavedStateRegistryOwner,
    ViewModelStoreOwner,
    LifecycleOwner by lifecycleOwner {

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()

    init {
        enableSavedStateHandles()
        savedStateRegistryController.performRestore(null)
    }
}
