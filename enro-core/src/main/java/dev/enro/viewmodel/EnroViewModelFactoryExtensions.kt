package dev.enro.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import dev.enro.core.NavigationHandle
import dev.enro.core.compose.navigationHandle

fun ViewModelProvider.Factory.withNavigationHandle(
    navigationHandle: NavigationHandle
): ViewModelProvider.Factory = EnroViewModelFactory(
    navigationHandle = navigationHandle,
    delegate = this
)

@Composable
fun ViewModelProvider.Factory.withNavigationHandle() = withNavigationHandle(
    navigationHandle = navigationHandle()
)