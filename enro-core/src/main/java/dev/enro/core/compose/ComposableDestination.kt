package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.compose.destination.ComposableDestinationOwner

public abstract class ComposableDestination : LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {
    internal lateinit var owner: ComposableDestinationOwner

    override val savedStateRegistry: SavedStateRegistry
        get() = owner.savedStateRegistry

    override fun getLifecycle(): Lifecycle {
        return owner.lifecycle
    }

    override fun getViewModelStore(): ViewModelStore {
        return owner.viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return owner.defaultViewModelProviderFactory
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return owner.defaultViewModelCreationExtras
    }

    @Composable
    public abstract fun Render()
}