package dev.enro.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public val LocalNavigationAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope> =
    compositionLocalOf { error("AnimatedContentScope not provided") }

/**
 * The scope of [LocalNavigationAnimatedVisibilityScope], or `null` when no scope is
 * provided: for example, a destination rendered outside a `NavigationDisplay` in a snapshot
 * test. `NavigationDisplay` provides both locals together.
 */
public val LocalNavigationAnimatedVisibilityScopeOrNull: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    compositionLocalOf { null }
