package dev.enro.viewmodel

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.EnroController

// Provides default creation extras and the controller's ViewModel factory for a
// ViewModelStoreOwner that does not implement HasDefaultViewModelProviderFactory.
internal class EnroWrappedViewModelStoreOwner(
    private val controller: EnroController,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    // A null savedStateRegistryOwner is replaced by an UnboundedSavedStateRegistryOwner, which
    // saves no state. Saving no state is acceptable on web and desktop.
    savedStateRegistryOwner: SavedStateRegistryOwner?,
) : ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    private val savedStateRegistryOwner = savedStateRegistryOwner ?: UnboundedSavedStateRegistryOwner(this)
    private val delegate = viewModelStoreOwner as? HasDefaultViewModelProviderFactory

    override val defaultViewModelCreationExtras: CreationExtras
        get() {
            if (delegate != null) return delegate.defaultViewModelCreationExtras
            return MutableCreationExtras().apply {
                set(SAVED_STATE_REGISTRY_OWNER_KEY, savedStateRegistryOwner)
                set(VIEW_MODEL_STORE_OWNER_KEY, viewModelStoreOwner)
            }
        }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            if (delegate != null) return delegate.defaultViewModelProviderFactory
            return controller.viewModelRepository.getFactory()
        }

    // Used when no SavedStateRegistryOwner is provided. Does not save or restore state.
    private class UnboundedSavedStateRegistryOwner(
        private val owner: EnroWrappedViewModelStoreOwner,
    ) : SavedStateRegistryOwner,
        LifecycleOwner,
        ViewModelStoreOwner by owner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle = lifecycleRegistry

        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

        init {
            enableSavedStateHandles()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }
    }
}