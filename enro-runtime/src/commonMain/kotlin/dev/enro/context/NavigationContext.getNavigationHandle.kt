package dev.enro.context

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.viewmodel.getNavigationHandle
import kotlin.jvm.JvmName

public inline fun <reified T : NavigationKey> AnyNavigationContext.getNavigationHandle(): NavigationHandle<T> {
    return (this as ViewModelStoreOwner).getNavigationHandle<T>()
}

@JvmName("getNavigationHandleDefault")
public fun AnyNavigationContext.getNavigationHandle(): NavigationHandle<NavigationKey> {
    return (this as ViewModelStoreOwner).getNavigationHandle<NavigationKey>()
}
