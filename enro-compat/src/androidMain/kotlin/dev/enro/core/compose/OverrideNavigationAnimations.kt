package dev.enro.core.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope


@Composable
@AdvancedEnroApi
@Deprecated(
    message = "OverrideNavigationAnimations is now a no-op and doesn't actually do anything. Navigation overrides should be set on the destination or on the container itself.",
    level = DeprecationLevel.WARNING
)
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
) {}

@Composable
@AdvancedEnroApi
@Deprecated(
    message = "OverrideNavigationAnimations is now a no-op and doesn't actually do anything. Navigation overrides should be set on the destination or on the container itself.",
    level = DeprecationLevel.WARNING
)
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    LocalNavigationAnimatedVisibilityScope.current.content()
}