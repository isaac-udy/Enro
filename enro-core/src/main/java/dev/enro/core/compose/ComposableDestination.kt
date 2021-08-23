package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.internal.handle.getNavigationHandleViewModel

abstract class ComposableDestination: LifecycleOwner, ViewModelStoreOwner {

    internal lateinit var instruction: NavigationInstruction.Open
    internal lateinit var activity: FragmentActivity
    internal lateinit var containerState: EnroContainerState
    internal lateinit var navigationHandle: NavigationHandle
    internal lateinit var lifecycleOwner: LifecycleOwner
    internal lateinit var viewModelStoreOwner: ViewModelStoreOwner

    internal var parentContext: NavigationContext<*>? = null

    override fun getLifecycle(): Lifecycle {
        return lifecycleOwner.lifecycle
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStoreOwner.viewModelStore
    }

    @Composable
    internal fun InternalRender() {
        navigationHandle = getNavigationHandleViewModel()
        CompositionLocalProvider(
            LocalNavigationHandle provides navigationHandle,
            LocalViewModelStoreOwner provides viewModelStoreOwner
        ) {
            Render()
        }
    }

    @Composable
    abstract fun Render()
}

internal class ComposableDestinationViewModelStoreOwner : ViewModel(), ViewModelStoreOwner {
    private val viewModelStore = ViewModelStore()

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }
}