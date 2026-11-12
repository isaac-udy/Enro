package dev.enro.ui

import androidx.compose.runtime.Immutable

/**
 * The receiver of [NavigationSceneStrategy.calculateScene]. Navigation 3 equivalent:
 * `SceneStrategyScope<T>`.
 */
@Immutable
public open class SceneStrategyScope internal constructor(
    /**
     * Call [onBack] when the user dismisses the scene from inside the scene, for example by
     * dragging a sheet down or tapping outside a dialog.
     *
     * [NavigationDisplay] requests a close of each destination in the container that is not in
     * the current scene's `previousEntries`. For any other navigation, use a
     * [dev.enro.NavigationHandle].
     */
    public val onBack: () -> Unit,
) {
    /**
     * Creates a [SceneStrategyScope] whose [onBack] does nothing, for calling a strategy
     * outside [NavigationDisplay] and [rememberNavigationSceneState], for example in a test.
     */
    public constructor() : this(onBack = {})
}
