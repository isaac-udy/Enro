package dev.enro.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * The destination IDs that should be SKIPPED when rendering in the current
 * [NavigationScene]. This is used by [movableContentDecorator] to ensure that
 * an entry which appears in more than one scene during a transition is only
 * actually composed by the scene that "wins" it under the z-order rules in
 * [NavigationDisplay].
 *
 * Mirrors Nav3's `LocalEntriesToExcludeFromCurrentScene`. If nothing has been
 * provided (e.g. a destination is rendered outside a `NavigationDisplay`),
 * the empty default means "exclude nothing" — every destination renders.
 */
public val LocalEntriesToExcludeFromCurrentScene: ProvidableCompositionLocal<Set<String>> =
    compositionLocalOf { emptySet() }
