package dev.enro.context

import androidx.compose.runtime.MutableState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

public class DestinationContext<out T : NavigationKey>(
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
    public override val parent: ContainerContext,
    public val destination: NavigationDestination<T>,
    activeChildId: MutableState<String?>,
) : NavigationContext.WithContainerChildren<ContainerContext>(activeChildId),
    LifecycleOwner by lifecycleOwner,
    ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory  {

    override val id: String get() = destination.id
    override val controller: EnroController = parent.controller

    public val key: T get() = destination.key
    public val instance: NavigationKey.Instance<T> get() = destination.instance
}