package dev.enro.destination.compose.dialog

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import dev.enro.destination.compose.OverrideNavigationAnimations

@Composable
public fun DialogDestination(content: @Composable () -> Unit) {
    OverrideNavigationAnimations(enter = EnterTransition.None, exit = ExitTransition.None)
    content()
}