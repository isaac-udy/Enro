package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import dev.enro.core.getNavigationHandle

public fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this
)

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