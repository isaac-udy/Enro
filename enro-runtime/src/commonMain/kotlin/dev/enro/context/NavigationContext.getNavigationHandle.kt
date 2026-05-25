package dev.enro.context

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.viewmodel.getNavigationHandle
import kotlin.jvm.JvmName

/**
 * Returns the [NavigationHandle] typed to [T] for this navigation
 * context, validating the key type at runtime.
 *
 * Most consumers should reach for the Composable / ViewModel accessors
 * instead. This is the right call when you're walking the navigation
 * context tree (e.g. inside a `NavigationContainer` traversal or a custom
 * back handler that inspects child contexts) and need a handle for a
 * specific child.
 */
public inline fun <reified T : NavigationKey> AnyNavigationContext.getNavigationHandle(): NavigationHandle<T> {
    return (this as ViewModelStoreOwner).getNavigationHandle<T>()
}

/**
 * Untyped variant of [getNavigationHandle] for code that needs to reach
 * the handle but doesn't care (or can't tell) what the destination's key
 * type is.
 */
@JvmName("getNavigationHandleDefault")
public fun AnyNavigationContext.getNavigationHandle(): NavigationHandle<NavigationKey> {
    return (this as ViewModelStoreOwner).getNavigationHandle<NavigationKey>()
}
