package dev.enro.context

import androidx.compose.runtime.MutableState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.EnroController

public class RootContext(
    override val id: String,
    override val parent: Any,
    override val controller: EnroController,
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
    private val activeChildId: MutableState<String?>,
) : NavigationContext.WithContainerChildren<Any>(activeChildId),
    LifecycleOwner by lifecycleOwner,
    ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory {

}