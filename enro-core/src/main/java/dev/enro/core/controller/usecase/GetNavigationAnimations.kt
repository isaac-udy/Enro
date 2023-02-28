package dev.enro.core.controller.usecase

import android.provider.Settings
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.DefaultAnimations
import dev.enro.core.NavigationAnimationTransition
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application

internal class GetNavigationAnimations(
    private val controller: NavigationController,
) {

    fun opening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition {
        if (earlyExitForNoAnimation()) return DefaultAnimations.noOp

        return DefaultAnimations.opening(exiting, entering)
    }

    fun closing(exiting: AnyOpenInstruction, entering: AnyOpenInstruction?): NavigationAnimationTransition {
        if (earlyExitForNoAnimation()) return DefaultAnimations.noOp

        return DefaultAnimations.closing(exiting, entering)
    }

    private fun earlyExitForNoAnimation() : Boolean {
        val animationScale = runCatching {
            Settings.Global.getFloat(
                controller.application.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE
            )
        }.getOrDefault(1.0f)

        return animationScale < 0.01f || controller.isAnimationsDisabled
    }
}
