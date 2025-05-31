package dev.enro3.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro3.NavigationContext
import dev.enro3.NavigationKey

public val LocalNavigationContext: ProvidableCompositionLocal<NavigationContext<out NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationContext")
    }