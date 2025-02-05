package dev.enro.core.compose.dialog

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import dev.enro.core.compose.OverrideNavigationAnimations

@Composable
public fun DialogDestination(content: @Composable AnimatedVisibilityScope.() -> Unit) {
    OverrideNavigationAnimations(enter = EnterTransition.None, exit = ExitTransition.None) {
        content()
    }
}