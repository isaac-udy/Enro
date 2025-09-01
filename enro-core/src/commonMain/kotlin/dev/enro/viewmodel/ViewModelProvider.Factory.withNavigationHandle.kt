package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import dev.enro.NavigationHandle
import dev.enro.navigationHandle


/**
 * Given a ViewModelProvider.Factory, wraps that factory as an EnroViewModelFactory with the current NavigationHandle provided
 * to ViewModels that are created with that factory, allowing the use of `by navigationHandle` in those ViewModels.
 */
public fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle<*>,
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this,
)

/**
 * A Composable helper for [withNavigationHandle] that automatically retrieves the current NavigationHandle from the Composition,
 * and remembers the result of applying withNavigationHandle.
 *
 * @see [withNavigationHandle]
 */
@Composable
public fun ViewModelProvider.Factory.withNavigationHandle(): ViewModelProvider.Factory {
    val navigation = navigationHandle()

    return remember(this, navigation) {
        withNavigationHandle(
            navigationHandle = navigation,
        )
    }
}