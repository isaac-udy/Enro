package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro3.NavigationHandle
import dev.enro3.NavigationKey
import dev.enro3.handle.NavigationHandleHolder

@PublishedApi
internal val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle<out NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationHandle")
    }

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
