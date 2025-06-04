package dev.enro.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro.NavigationContext
import dev.enro.NavigationKey

public val LocalNavigationContext: ProvidableCompositionLocal<NavigationContext<out NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationContext")
    }