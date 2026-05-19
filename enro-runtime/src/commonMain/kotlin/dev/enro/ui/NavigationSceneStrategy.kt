package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import dev.enro.NavigationKey

@Stable
public fun interface NavigationSceneStrategy {
    /**
     * Try to construct a [NavigationScene] from the given [entries]. Return
     * `null` to defer to the next strategy in the chain. The receiver
     * [SceneStrategyScope] gives the strategy a callback to dispatch back
     * navigation back into the surrounding [NavigationDisplay].
     *
     * Mirrors Nav3's `SceneStrategy<T>.calculateScene` shape (receiver +
     * entries argument).
     */
    @Composable
    public fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene?

    public companion object {
        /**
         * Chains a list of strategies so the first non-null result wins.
         * Mirrors the `List<SceneStrategy<T>>` overloads of `NavDisplay`.
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