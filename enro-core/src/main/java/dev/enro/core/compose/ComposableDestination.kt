package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.ComposeContext
import dev.enro.core.compose.destination.ComposableDestinationOwner

public abstract class ComposableDestination : LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {
    internal lateinit var owner: ComposableDestinationOwner
    internal val context by lazy { ComposeContext(this) }

    override val savedStateRegistry: SavedStateRegistry
        get() = owner.savedStateRegistry

    override val lifecycle: Lifecycle get() {
        return owner.lifecycle
    }

    override val viewModelStore: ViewModelStore get() {
        return owner.viewModelStore
    }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() {
        return owner.defaultViewModelProviderFactory
    }

    override val defaultViewModelCreationExtras: CreationExtras get() {
        return owner.defaultViewModelCreationExtras
    }

    @Composable
    public abstract fun Render()
}