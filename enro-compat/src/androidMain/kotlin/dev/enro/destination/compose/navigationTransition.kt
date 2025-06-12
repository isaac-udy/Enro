package dev.enro.destination.compose

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope

@AdvancedEnroApi
public val navigationTransition: Transition<EnterExitState>
    @Composable
    get() {
        return LocalNavigationAnimatedVisibilityScope.current.transition
    }