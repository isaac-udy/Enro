package dev.enro.core

import androidx.compose.ui.window.ApplicationScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated

public fun DesktopApplicationContext(
    contextReference: ApplicationScope,
    navigationController: NavigationController,
): NavigationContext<ApplicationScope> {
    val owners = object : SavedStateRegistryOwner, ViewModelStoreOwner {
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
        override val viewModelStore: ViewModelStore = ViewModelStore()
        private val lifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = lifecycleRegistry

        init {
            enableSavedStateHandles()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }
    }
    return NavigationContext(
        contextReference = contextReference,
        getController = { navigationController },
        getParentContext = { null },
        getArguments = { savedState() },
        getViewModelStoreOwner = { owners },
        getSavedStateRegistryOwner = { owners },
        getLifecycleOwner = { owners },
        onBoundToNavigationHandle = { }
    ).apply {
        // TODO: Handle multi-window situations with the activity navigation containers,
        // so that the correct container is used for the correct activity, and that it is possible
        // to traverse through the different windows.
        controller.dependencyScope.get<OnNavigationContextCreated>()
            .invoke(this, null)
    }
}