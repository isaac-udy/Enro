package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import dev.enro.NavigationKey

@Stable
public fun interface NavigationSceneStrategy {
    @Composable
    public fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
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