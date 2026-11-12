package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import dev.enro.NavigationHandle
import dev.enro.navigationHandle


/**
 * Returns a factory that creates each ViewModel with this factory and provides
 * [navigationHandle] to the ViewModel, so that the ViewModel can declare `by navigationHandle`.
 */
public fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle<*>,
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this,
)

/**
 * [withNavigationHandle] with the [NavigationHandle] of the destination that contains this
 * Composable. The returned factory is remembered.
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