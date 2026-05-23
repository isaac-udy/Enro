package dev.enro.recipes.scenedecoration.complex

import dev.enro.ui.NavigationDestination
import dev.enro.ui.get

/**
 * Metadata vocabulary for the Shell Scene recipe.
 *
 * Destinations declare what pane / overlay shape they support by adding
 * these tags in their `navigationDestination(metadata = { ... })` block.
 * The [ShellPaneSceneStrategy] reads them while resolving slots, and the
 * decorator reads [isFullScreen] / [isLeftPane] / [isRightPane] when deciding
 * whether to hide the mobile bottom chrome.
 *
 * `directOverlay()` is intentionally **not** redefined here — the recipe
 * reuses the existing builder from `dev.enro.ui.scenes` so destinations
 * already tagged for the built-in `DirectOverlaySceneStrategy` work
 * unchanged. The recipe just registers [ShellOverlaySceneStrategy] earlier
 * in the chain so it gets to render them with shell-specific positioning
 * (drawer on desktop, sheet on mobile).
 *
 * There is also intentionally no `primary()` tag — any untagged
 * destination occupies the main slot when it's the right one to do so.
 */
internal object ShellPaneMetadata {
    // Explicit `internal` is needed (not implied by the enclosing internal
    // object) for K/Native's ObjC export — NavigationDestination.MetadataKey
    // is a generic type and gets auto-marked @HiddenFromObjC by the
    // compiler, and public subtypes of a HiddenFromObjC type are rejected.
    internal object IsLeftPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)
    internal object IsRightPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)
    internal object IsFullScreenKey : NavigationDestination.MetadataKey<Boolean>(default = false)
}

/** "I can render as the **left companion** when something is pushed on top of me." */
internal fun NavigationDestination.MetadataBuilder<*>.leftPane() {
    add(ShellPaneMetadata.IsLeftPaneKey, true)
}

/** "I can render as the **right companion** when I'm on top of the backstack." */
internal fun NavigationDestination.MetadataBuilder<*>.rightPane() {
    add(ShellPaneMetadata.IsRightPaneKey, true)
}

/** "I claim the entire content area — no pane companions, and hide the mobile bottom chrome." */
internal fun NavigationDestination.MetadataBuilder<*>.fullScreen() {
    add(ShellPaneMetadata.IsFullScreenKey, true)
}

internal val NavigationDestination<*>.isLeftPane: Boolean
    get() = metadata[ShellPaneMetadata.IsLeftPaneKey]

internal val NavigationDestination<*>.isRightPane: Boolean
    get() = metadata[ShellPaneMetadata.IsRightPaneKey]

internal val NavigationDestination<*>.isFullScreen: Boolean
    get() = metadata[ShellPaneMetadata.IsFullScreenKey]
