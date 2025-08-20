package dev.enro.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro.NavigationHandle
import dev.enro.NavigationKey

// TODO update to work like LocalNavigationContext, and look for root context
@PublishedApi
internal val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle<NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationHandle")
    }

