package dev.enro.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
public val LocalNavigationSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    compositionLocalOf { error("SharedTransitionScope not provided")}

/**
 * Nullable mirror of [LocalNavigationSharedTransitionScope] for
 * callers that want to gracefully degrade when no scope is available
 * (e.g. design-system snapshot tests rendering a destination
 * standalone, outside any `NavigationDisplay`). Provided alongside
 * the strict local everywhere the strict local is provided — reading
 * this returns `null` when nothing's been set, rather than throwing.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
public val LocalNavigationSharedTransitionScopeOrNull: ProvidableCompositionLocal<SharedTransitionScope?> =
    compositionLocalOf { null }
