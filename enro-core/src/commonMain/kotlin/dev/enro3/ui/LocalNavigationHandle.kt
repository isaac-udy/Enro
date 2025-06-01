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

