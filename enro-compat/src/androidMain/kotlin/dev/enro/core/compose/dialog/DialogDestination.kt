package dev.enro.core.compose.dialog

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope

@Composable
public fun DialogDestination(content: @Composable AnimatedVisibilityScope.() -> Unit) {
    content(LocalNavigationAnimatedVisibilityScope.current)
}