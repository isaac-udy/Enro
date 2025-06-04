package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.handle.NavigationHandleHolder

@PublishedApi
internal val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle<out NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationHandle")
    }

