package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import dev.enro.core.ArchitectureException
import dev.enro.core.NavigationHandle
import dev.enro.core.navigationHandle

public fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this
)

@ArchitectureException("withNavigationHandle needs to access navigationHandle from the compose package")
private object ComposableNavigationHandle {
    @Composable
    fun get() = navigationHandle()
}

@Composable
public fun ViewModelProvider.Factory.withNavigationHandle(): ViewModelProvider.Factory {
    val navigationHandle = ComposableNavigationHandle.get()
    return remember(this, navigationHandle) {
        withNavigationHandle(
            navigationHandle = navigationHandle
        )
    }
}