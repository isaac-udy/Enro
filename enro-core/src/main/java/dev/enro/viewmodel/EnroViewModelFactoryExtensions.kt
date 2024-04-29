package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import dev.enro.core.getNavigationHandle

/**
 * Given a ViewModelProvider.Factory, wraps that factory as an EnroViewModelFactory with the current NavigationHandle provided
 * to ViewModels that are created with that factory, allowing the use of `by navigationHandle` in those ViewModels.
 */
public fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this
)

/**
 * A Composable helper for [withNavigationHandle] that automatically retrieves the current NavigationHandle from the Composition,
 * and remembers the result of applying withNavigationHandle.
 *
 * @see [withNavigationHandle]
 */
@Composable
public fun ViewModelProvider.Factory.withNavigationHandle(): ViewModelProvider.Factory {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current

    return remember(this, viewModelStoreOwner) {
        if(viewModelStoreOwner == null) throw EnroException.UnreachableState()
        val navigationHandle = viewModelStoreOwner.getNavigationHandle()

        withNavigationHandle(
            navigationHandle = navigationHandle
        )
    }
}