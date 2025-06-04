package dev.enro.ui

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey

public fun interface NavigationSceneStrategy {
    @Composable
    public fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
    ): NavigationScene?

    public companion object {
        public fun from(
            vararg sceneStrategies: NavigationSceneStrategy,
        ): NavigationSceneStrategy {
            return NavigationSceneStrategy { entries ->
                sceneStrategies.firstNotNullOfOrNull { it.calculateScene(entries) }
            }
        }
    }
}