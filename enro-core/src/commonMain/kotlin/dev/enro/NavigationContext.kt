package dev.enro

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.ui.NavigationDestination

public class NavigationContext<T : NavigationKey>(
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
    public val destination: NavigationDestination<T>,
    public val parentContainer: NavigationContainer,
) : LifecycleOwner by lifecycleOwner,
    ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory {

    private val mutableChildContainers = mutableListOf<NavigationContainer>()
    public val childContainers: List<NavigationContainer> get() = mutableChildContainers.toList()

    internal fun registerChildContainer(container: NavigationContainer) {
        mutableChildContainers.add(container)
    }

    internal fun unregisterChildContainer(container: NavigationContainer) {
        mutableChildContainers.remove(container)
    }
}

