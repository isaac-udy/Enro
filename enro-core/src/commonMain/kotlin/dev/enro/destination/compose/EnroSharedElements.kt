package dev.enro.destination.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

public val LocalAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope> =
    staticCompositionLocalOf {
        error("No AnimatedVisibilityScope provided")
    }

@OptIn(ExperimentalSharedTransitionApi::class)
public val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    staticCompositionLocalOf {
        error("No SharedTransitionScope provided")
    }

@ExperimentalSharedTransitionApi
public class EnroSharedTransitionScope(
    private val sharedTransitionScope: SharedTransitionScope,
    public val animatedVisibilityScope: AnimatedVisibilityScope,
) : SharedTransitionScope by sharedTransitionScope

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
public fun EnroSharedElements(
    content: @Composable EnroSharedTransitionScope.() -> Unit,
) {
    with(
        EnroSharedTransitionScope(
            sharedTransitionScope = LocalSharedTransitionScope.current,
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
        )
    ) {
        content()
    }
}