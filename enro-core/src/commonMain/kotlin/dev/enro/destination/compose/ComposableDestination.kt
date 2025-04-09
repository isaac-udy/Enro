package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.ComposableDestinationReference
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.getNavigationHandle


public abstract class ComposableDestination : LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory,
    ComposableDestinationReference {

    internal lateinit var owner: ComposableDestinationOwner
    internal val context by lazy { ComposeContext(this) }

    override val savedStateRegistry: SavedStateRegistry
        get() = owner.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = owner.lifecycle

    override val viewModelStore: ViewModelStore
        get() = owner.viewModelStore

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = owner.defaultViewModelProviderFactory

    override val defaultViewModelCreationExtras: CreationExtras
        get() = owner.defaultViewModelCreationExtras

    override val navigationContext: NavigationContext<*>
        get() = context

    /**
     * Renders the content of this composable destination
     */
    @Composable
    public abstract fun Render()

    /**
     * Gets the navigation handle for this composable destination
     */
    public fun getNavigationHandle(): NavigationHandle {
        return owner.getNavigationHandle()
    }
}