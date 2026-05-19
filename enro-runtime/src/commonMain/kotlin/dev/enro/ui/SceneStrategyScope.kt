package dev.enro.ui

import androidx.compose.runtime.Immutable

/**
 * Scope passed into [NavigationSceneStrategy.calculateScene] so the
 * strategy can plug back-handling done inside its scene (custom
 * gestures, "X" close buttons in a dialog, drag-to-dismiss sheets)
 * into the surrounding [NavigationDisplay]'s navigation event system.
 *
 * Mirrors Nav3's `SceneStrategyScope<T>`.
 */
@Immutable
public open class SceneStrategyScope internal constructor(
    /**
     * Invoke when the scene wants to pop the current top entry —
     * typically because the user dismissed the scene through its
     * internal affordance (drag-down on a sheet, tap-outside on a
     * dialog, custom back gesture).
     *
     * The surrounding [NavigationDisplay] receives the event and
     * applies it to the backstack the same way a hardware back press
     * would. If you need different semantics (close a flow, navigate
     * to a sibling), use a [dev.enro.NavigationHandle] directly
     * instead.
     */
    public val onBack: () -> Unit,
) {
    /**
     * Constructs a [SceneStrategyScope] suitable for calling a
     * strategy in isolation (e.g. tests). Real consumers get one
     * automatically from [NavigationDisplay] /
     * [rememberNavigationSceneState].
     */
    public constructor() : this(onBack = {})
}
