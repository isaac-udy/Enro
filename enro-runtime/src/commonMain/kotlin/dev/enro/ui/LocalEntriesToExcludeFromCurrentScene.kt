package dev.enro.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * The ids of the destinations that the current [NavigationScene] must not render. During a
 * transition a destination can be in more than one scene; [NavigationDisplay] provides this
 * set so that `movableContentDecorator` composes the destination's content in one scene only.
 *
 * The default is an empty set, so a destination rendered outside a [NavigationDisplay] always
 * renders. Navigation 3 equivalent: `LocalEntriesToExcludeFromCurrentScene`.
 */
public val LocalEntriesToExcludeFromCurrentScene: ProvidableCompositionLocal<Set<String>> =
    compositionLocalOf { emptySet() }
