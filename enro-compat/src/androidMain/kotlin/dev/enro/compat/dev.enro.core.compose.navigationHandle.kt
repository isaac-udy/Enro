package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.NavigationHandle
import dev.enro.NavigationKey

@Composable
public fun navigationHandle(): NavigationHandle<out NavigationKey> {
    return dev.enro.navigationHandle()
}

@JvmName("typedNavigationHandle")
@Composable
public inline fun <reified T : NavigationKey> navigationHandle(): NavigationHandle<T> {
    return dev.enro.navigationHandle<T>()
}
