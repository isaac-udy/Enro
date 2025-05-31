package dev.enro3.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro3.NavigationKey
import dev.enro3.NavigationHandle

public val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle<out NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationHandle")
    }