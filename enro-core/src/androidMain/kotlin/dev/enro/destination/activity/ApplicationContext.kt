package dev.enro.destination.activity

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro.core.NavigationContext
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated

internal fun ApplicationContext(
    contextReference: Application,
    navigationController: NavigationController,
): NavigationContext<Application> {
    val owners = object : SavedStateRegistryOwner, ViewModelStoreOwner {
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
        override val viewModelStore: ViewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle get() = ProcessLifecycleOwner.get().lifecycle

        init {
            enableSavedStateHandles()
            savedStateRegistryController.performRestore(null)
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
        containerManager.addContainer(ActivityNavigationContainer(this))
        controller.dependencyScope.get<OnNavigationContextCreated>()
            .invoke(this, null)
    }
}