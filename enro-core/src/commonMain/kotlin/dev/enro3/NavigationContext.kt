package dev.enro3

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro3.ui.NavigationDestination

public class NavigationContext<T : NavigationKey>(
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,

    public val destination: NavigationDestination<T>,
    public val parentContainer: NavigationContainer,

    // TODO need some kind of ContainerManager type thing for child containers
    public val childContainers: List<NavigationContainer>,
) : LifecycleOwner by lifecycleOwner,
    ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory

