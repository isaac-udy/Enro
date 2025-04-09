package dev.enro.core.compose.destination

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedState
import dev.enro.core.getNavigationHandle
import dev.enro.viewmodel.withNavigationHandle

internal class ComposableDestinationViewModelStoreOwner(
    private val owner: ComposableDestinationOwner,
    private val savedState: SavedState,
    override val viewModelStore: ViewModelStore,
): ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    init {
        owner.enableSavedStateHandles()
    }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() {
        val factory = createViewModelFactory(
            owner = owner,
            savedState = savedState,
        )
        return factory.withNavigationHandle(getNavigationHandle())
    }

    override val defaultViewModelCreationExtras: CreationExtras get() {
        return MutableCreationExtras().apply {
            addPlatformExtras(owner)
            set(SAVED_STATE_REGISTRY_OWNER_KEY, owner)
            set(VIEW_MODEL_STORE_OWNER_KEY, owner)
        }
    }
}

internal expect fun createViewModelFactory(
    owner: ComposableDestinationOwner,
    savedState: SavedState,
) : ViewModelProvider.Factory

internal expect fun MutableCreationExtras.addPlatformExtras(
    owner: ComposableDestinationOwner,
)