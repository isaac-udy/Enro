package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import dev.enro.NavigationKey

@Stable
public fun interface NavigationSceneStrategy {
    /**
     * Returns a [NavigationScene] for [entries], or `null` to pass [entries] to the next
     * strategy. [entries] is never empty when called by [rememberNavigationSceneState].
     *
     * Navigation 3 equivalent: `SceneStrategy<T>.calculateScene`.
     */
    @Composable
    public fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene?

    public companion object {
        /**
         * Returns a strategy that calls each of [sceneStrategies] in order and returns the
         * first non-null scene.
         */
        public fun from(
            sceneStrategies: List<NavigationSceneStrategy>,
        ): NavigationSceneStrategy {
            return NavigationSceneStrategy { entries ->
                val scope = this
                sceneStrategies.firstNotNullOfOrNull { strategy ->
                    with(strategy) { scope.calculateScene(entries) }
                }
            }
        }

        public fun from(
            vararg sceneStrategies: NavigationSceneStrategy,
        ): NavigationSceneStrategy = from(sceneStrategies.toList())
    }
}