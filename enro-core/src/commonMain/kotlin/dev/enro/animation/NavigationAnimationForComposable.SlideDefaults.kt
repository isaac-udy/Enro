package dev.enro.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

private val slideDefaults = NavigationAnimation.Defaults(
    none = NavigationAnimationForComposable(
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ),
    push = NavigationAnimationForComposable(
        enter = fadeIn() + slideInHorizontally { it / 3 },
        exit = fadeOut() + slideOutHorizontally { -it / 6 },
    ),
    pushReturn = NavigationAnimationForComposable(
        enter = fadeIn() + slideInHorizontally { -(it) / 6 },
        exit = fadeOut() + slideOutHorizontally { it / 3 },
    ),
    present = NavigationAnimationForComposable(
        enter = fadeIn() + slideInVertically { -(it) / 6 },
        exit = fadeOut() + slideOutVertically { it / 3 },
    ),
    presentReturn = NavigationAnimationForComposable(
        enter = fadeIn() + slideInVertically { -(it) / 6 },
        exit = fadeOut() + slideOutVertically { it / 3 },
    ),
)

public val NavigationAnimationForComposable.Companion.SlideDefaults: NavigationAnimation.Defaults<NavigationAnimationForComposable>
    get() = slideDefaults