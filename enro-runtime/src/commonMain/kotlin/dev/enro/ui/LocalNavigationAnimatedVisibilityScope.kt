package dev.enro.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public val LocalNavigationAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope> =
    compositionLocalOf { error("AnimatedContentScope not provided") }

/**
 * Nullable mirror of [LocalNavigationAnimatedVisibilityScope] for
 * callers that want to gracefully degrade when no overlay scope is
 * available (e.g. design-system snapshot tests rendering a dialog
 * standalone, outside any `NavigationDisplay`).
 *
 * Provided alongside the strict local everywhere the strict local is
 * provided — reading this returns `null` when nothing's been set,
 * rather than throwing.
 */
public val LocalNavigationAnimatedVisibilityScopeOrNull: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    compositionLocalOf { null }
