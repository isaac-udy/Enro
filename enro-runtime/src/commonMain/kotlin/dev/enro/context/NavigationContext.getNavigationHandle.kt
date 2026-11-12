package dev.enro.context

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.viewmodel.getNavigationHandle
import kotlin.jvm.JvmName

/**
 * Returns the [NavigationHandle] of this navigation context, typed to [T]. Throws if the
 * key of the [NavigationHandle] is not a [T].
 */
public inline fun <reified T : NavigationKey> AnyNavigationContext.getNavigationHandle(): NavigationHandle<T> {
    return (this as ViewModelStoreOwner).getNavigationHandle<T>()
}

/**
 * Returns the untyped [NavigationHandle] of this navigation context.
 */
@JvmName("getNavigationHandleDefault")
public fun AnyNavigationContext.getNavigationHandle(): NavigationHandle<NavigationKey> {
    return (this as ViewModelStoreOwner).getNavigationHandle<NavigationKey>()
}
