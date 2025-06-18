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

// Wraps a ViewModelStoreOwner and a SavedStateRegistryOwner to
// ensure that Enro-required extras/factory stuff is configured
internal class EnroWrappedViewModelStoreOwner(
    private val controller: EnroController,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    // The savedStateRegistryOwner to use for this ViewModelStoreOwner's saved state handles and other things,
    // it's OK to provide null here, but doing so will create an UnboundedSavedStateRegistryOwner, which won't
    // actually save any state (which is fine for some platforms, like web/desktop.
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

    // This is a saved state registry owner for use when there is no other saved state registry provider actually
    // provided, which is mostly useful on non-Android platforms where saving is not as important. This
    // registry owner will not actually save or restore any state as it currently stands
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