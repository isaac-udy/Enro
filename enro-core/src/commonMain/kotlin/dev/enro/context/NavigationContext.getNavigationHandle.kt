package dev.enro.context

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.viewmodel.getNavigationHandle

public inline fun <reified T : NavigationKey> DestinationContext<T>.getNavigationHandle(): NavigationHandle<T> {
    return (this as ViewModelStoreOwner).getNavigationHandle<T>()
}

public inline fun <reified T : NavigationKey> ContainerContext.getActiveNavigationHandle(): NavigationHandle<T>? {
    return activeChild?.getNavigationHandle<T>()
}