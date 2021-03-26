package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped

val LocalNavigationHandle = compositionLocalOf<NavigationHandle> {
    throw IllegalStateException("The current composition does not have a NavigationHandle attached")
}

@Composable
inline fun <reified T: NavigationKey> navigationHandle(): TypedNavigationHandle<T> {
    return LocalNavigationHandle.current.asTyped()
}

@Composable
fun navigationHandle(): NavigationHandle {
    return LocalNavigationHandle.current
}