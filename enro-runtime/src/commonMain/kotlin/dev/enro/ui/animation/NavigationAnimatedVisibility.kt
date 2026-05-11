package dev.enro.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope

@Composable
public fun NavigationAnimatedVisibility(
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = shrinkOut() + fadeOut(),
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    LocalNavigationAnimatedVisibilityScope.current.transition.AnimatedVisibility(
        visible = { it == EnterExitState.Visible },
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content,
    )
}