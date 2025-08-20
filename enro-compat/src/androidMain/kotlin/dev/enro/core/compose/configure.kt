package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.NavigationHandle
import dev.enro.NavigationHandleConfiguration
import dev.enro.NavigationKey
import dev.enro.configure as realConfigure

@Composable
public inline fun <reified T : NavigationKey> NavigationHandle<T>.configure(
    crossinline block: NavigationHandleConfiguration<T>.() -> Unit
) : NavigationHandle<T> {
    realConfigure {
        block()
    }
    return this
}