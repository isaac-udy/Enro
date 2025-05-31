package dev.enro3.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro3.NavigationContainer

public val LocalNavigationContainer: ProvidableCompositionLocal<NavigationContainer> = staticCompositionLocalOf {
    error("No LocalNavigationContainer")
}