package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import dev.enro.core.NavigationHandle
import dev.enro.core.compose.navigationHandle

public fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this
)

@Composable
public fun ViewModelProvider.Factory.withNavigationHandle(): ViewModelProvider.Factory {
    val navigationHandle = navigationHandle()
    return remember(this, navigationHandle) {
        withNavigationHandle(
            navigationHandle = navigationHandle
        )
    }
}