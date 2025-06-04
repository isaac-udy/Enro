package dev.enro.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro.NavigationContainer

public val LocalNavigationContainer: ProvidableCompositionLocal<NavigationContainer> = staticCompositionLocalOf {
    error("No LocalNavigationContainer")
}