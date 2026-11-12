package dev.enro.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
public val LocalNavigationSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    compositionLocalOf { error("SharedTransitionScope not provided")}

/**
 * The scope of [LocalNavigationSharedTransitionScope], or `null` when no scope is
 * provided: for example, a destination rendered outside a `NavigationDisplay` in a snapshot
 * test. `NavigationDisplay` provides both locals together.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
public val LocalNavigationSharedTransitionScopeOrNull: ProvidableCompositionLocal<SharedTransitionScope?> =
    compositionLocalOf { null }
