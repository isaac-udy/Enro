package dev.enro.core

import androidx.compose.runtime.Composable
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.container.NavigationContainer

public abstract class ComposableDestination : LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {
    internal lateinit var instruction: NavigationInstruction.Open<*>
    internal lateinit var parentContainer: NavigationContainer
    internal lateinit var lifecycleOwner: LifecycleOwner
    internal lateinit var viewModelStoreOwner: ViewModelStoreOwner
    internal lateinit var savedStateRegistryOwner: SavedStateRegistryOwner
    internal lateinit var hasDefaultViewModelProviderFactory: HasDefaultViewModelProviderFactory
    internal lateinit var context: NavigationContext<ComposableDestination>

    internal val activity by lazy { parentContainer.parentContext.activity }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryOwner.savedStateRegistry

    override fun getLifecycle(): Lifecycle {
        return lifecycleOwner.lifecycle
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStoreOwner.viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return hasDefaultViewModelProviderFactory.defaultViewModelProviderFactory
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return hasDefaultViewModelProviderFactory.defaultViewModelCreationExtras
    }

    @Composable
    public abstract fun Render()
}