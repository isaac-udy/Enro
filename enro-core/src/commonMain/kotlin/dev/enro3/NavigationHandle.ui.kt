package dev.enro3

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro3.handle.NavigationHandleHolder
import dev.enro3.ui.LocalNavigationContext

@Composable
public inline fun <reified T: NavigationKey> navigationHandle(): NavigationHandle<T> {
    val holder = viewModel<NavigationHandleHolder<T>>(
        viewModelStoreOwner = LocalNavigationContext.current,
    ) {
        error("No NavigationHandle found for ${T::class}")
    }

    return holder.navigationHandle.also { navigationHandle ->
        @Suppress("USELESS_IS_CHECK")
        require(navigationHandle.instance.key is T) {
            "Expected key of type ${T::class}, but found ${navigationHandle.instance.key::class}"
        }
    }
}