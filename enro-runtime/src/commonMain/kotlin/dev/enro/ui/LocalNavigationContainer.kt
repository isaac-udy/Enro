package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

public object LocalNavigationContainer {
    private val LocalNavigationContainer: ProvidableCompositionLocal<NavigationContainerState?> = staticCompositionLocalOf {
        null
    }

    public val current: NavigationContainerState
        @Composable get() {
            return LocalNavigationContainer.current ?: error("No LocalNavigationContainer (you might be calling this from a RootContext)")
        }

    public val currentOrNull: NavigationContainerState?
        @Composable get() = LocalNavigationContainer.current

    public infix fun provides(
        navigationContainerState: NavigationContainerState
    ): ProvidedValue<NavigationContainerState> {
        @Suppress("UNCHECKED_CAST")
        return LocalNavigationContainer.provides(navigationContainerState) as ProvidedValue<NavigationContainerState>
    }
}