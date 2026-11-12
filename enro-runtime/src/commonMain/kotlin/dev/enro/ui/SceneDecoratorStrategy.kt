package dev.enro.ui

import androidx.compose.runtime.Composable

/**
 * The receiver of [SceneDecoratorStrategy.decorateScene]. A scene decorator strategy calls
 * [onBack] to send a back event to the enclosing [NavigationDisplay].
 *
 * Navigation 3 equivalent: `SceneDecoratorStrategyScope<T>`.
 */
public class SceneDecoratorStrategyScope internal constructor(
    onBack: () -> Unit,
) : SceneStrategyScope(onBack) {
    public constructor() : this(onBack = {})
}

/**
 * Wraps a [NavigationScene] in another [NavigationScene], for example to render a navigation
 * drawer, app bar or navigation rail around the wrapped scene.
 *
 * Scene decorator strategies run after the [NavigationSceneStrategy] has selected a scene, in
 * list order: the first strategy produces the innermost wrapper. A [NavigationScene.Overlay] is
 * never decorated.
 *
 * Navigation 3 equivalent: `SceneDecoratorStrategy<T>`.
 */
public fun interface SceneDecoratorStrategy {
    /**
     * Returns the scene to render in place of [scene]. Return [scene] unchanged to skip
     * decoration.
     */
    @Composable
    public fun SceneDecoratorStrategyScope.decorateScene(scene: NavigationScene): NavigationScene
}
