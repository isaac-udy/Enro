package dev.enro.ui

import androidx.compose.runtime.Composable

/**
 * Scope passed into [SceneDecoratorStrategy.decorateScene]. Extends
 * [SceneStrategyScope] so a scene decorator strategy can plug its own
 * back-handling into the surrounding [NavigationDisplay] (e.g. a
 * navigation-drawer decorator that handles its own drawer-close
 * gesture).
 *
 * Mirrors Nav3's `SceneDecoratorStrategyScope<T>`.
 */
public class SceneDecoratorStrategyScope internal constructor(
    onBack: () -> Unit,
) : SceneStrategyScope(onBack) {
    public constructor() : this(onBack = {})
}

/**
 * A strategy that wraps a [NavigationScene] in another [NavigationScene],
 * typically to layer chrome around it (navigation drawer, app bar, nav
 * rail, side sheet — anything that owns layout space but defers actual
 * entry rendering to the wrapped inner scene).
 *
 * Decorator strategies are applied AFTER [NavigationSceneStrategy]
 * chains have selected the scene for the current entries, and ONLY to
 * non-overlay scenes. Overlays are animated separately from the main
 * scene so wrapping them in chrome wouldn't compose meaningfully.
 *
 * Mirrors Nav3's `SceneDecoratorStrategy<T>`.
 */
public fun interface SceneDecoratorStrategy {
    /**
     * Decorates [scene] and returns the (possibly wrapped) scene to
     * render. Return [scene] unchanged to skip decoration.
     */
    @Composable
    public fun SceneDecoratorStrategyScope.decorateScene(scene: NavigationScene): NavigationScene
}
