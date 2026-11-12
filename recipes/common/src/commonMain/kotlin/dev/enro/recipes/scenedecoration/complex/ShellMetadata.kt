package dev.enro.recipes.scenedecoration.complex

import dev.enro.ui.NavigationDestination
import dev.enro.ui.get

/**
 * The metadata keys of the Shell Scene recipe.
 *
 * A destination sets a key in the `navigationDestination(metadata = { ... })`
 * block with [leftPane], [rightPane] or [fullScreen]. [ShellPaneSceneStrategy]
 * reads the keys to assign slots. [ShellSceneDecorator] reads the keys to
 * hide the bottom chrome on Mobile.
 *
 * The recipe declares no overlay key: [ShellOverlaySceneStrategy] reads the
 * `directOverlay()` metadata from `dev.enro.ui.scenes`. The recipe declares
 * no key for the main slot: a destination with no pane metadata fills the
 * main slot.
 */
internal object ShellPaneMetadata {
    // The explicit `internal` is required for Kotlin/Native ObjC export: the
    // compiler marks the generic NavigationDestination.MetadataKey as
    // @HiddenFromObjC and rejects a public subtype of a @HiddenFromObjC type.
    internal object IsLeftPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)
    internal object IsRightPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)
    internal object IsFullScreenKey : NavigationDestination.MetadataKey<Boolean>(default = false)
}

/** The destination can fill the left slot when the destination is directly below the main slot. */
internal fun NavigationDestination.MetadataBuilder<*>.leftPane() {
    add(ShellPaneMetadata.IsLeftPaneKey, true)
}

/** The destination fills the right slot when the destination is the top entry. */
internal fun NavigationDestination.MetadataBuilder<*>.rightPane() {
    add(ShellPaneMetadata.IsRightPaneKey, true)
}

/** The destination fills the content area with no side panes, and hides the bottom chrome on Mobile. */
internal fun NavigationDestination.MetadataBuilder<*>.fullScreen() {
    add(ShellPaneMetadata.IsFullScreenKey, true)
}

internal val NavigationDestination<*>.isLeftPane: Boolean
    get() = metadata[ShellPaneMetadata.IsLeftPaneKey]

internal val NavigationDestination<*>.isRightPane: Boolean
    get() = metadata[ShellPaneMetadata.IsRightPaneKey]

internal val NavigationDestination<*>.isFullScreen: Boolean
    get() = metadata[ShellPaneMetadata.IsFullScreenKey]
