package dev.enro.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

public val LocalNavigationContainer: ProvidableCompositionLocal<NavigationContainerState> = staticCompositionLocalOf {
    error("No LocalNavigationContainer (you might be calling this from a RootContext)")
}