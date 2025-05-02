package dev.enro.core.controller.usecase

import dev.enro.animation.NavigationAnimation
import dev.enro.animation.NavigationAnimationOverride
import dev.enro.animation.findDefaults
import dev.enro.animation.findOverrideForClosing
import dev.enro.animation.findOverrideForOpening
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.controller.NavigationController
import kotlin.reflect.KClass

internal class GetNavigationAnimations(
    private val controller: NavigationController,
    internal val navigationAnimationOverride: NavigationAnimationOverride,
) {
    fun <T: NavigationAnimation> opening(
        type: KClass<T>,
        exiting: AnyOpenInstruction?,
        entering: AnyOpenInstruction,
    ): T {
        if (earlyExitForNoAnimation()) return navigationAnimationOverride.findDefaults(type).none
        val override = navigationAnimationOverride.findOverrideForOpening(
            type = type,
            exiting = exiting,
            entering = entering,
        )
        if (override != null) return override
        return when (entering.navigationDirection) {
            NavigationDirection.Present -> navigationAnimationOverride.findDefaults(type).present
            NavigationDirection.Push -> navigationAnimationOverride.findDefaults(type).push
        }
    }

    fun <T: NavigationAnimation> closing(
        type: KClass<T>,
        exiting: AnyOpenInstruction,
        entering: AnyOpenInstruction?
    ): T {
        if (earlyExitForNoAnimation()) return navigationAnimationOverride.findDefaults(type).none
        val override = navigationAnimationOverride.findOverrideForClosing(
            type = type,
            exiting = exiting,
            entering = entering,
        )
        if (override != null) return override
        return when (exiting.navigationDirection) {
            NavigationDirection.Present -> navigationAnimationOverride.findDefaults(type).presentReturn
            NavigationDirection.Push -> navigationAnimationOverride.findDefaults(type).pushReturn
        }
    }

    private fun earlyExitForNoAnimation() : Boolean {
        return isAnimationsDisabledForPlatform(controller) || controller.config.isAnimationsDisabled
    }
}

internal expect fun isAnimationsDisabledForPlatform(controller: NavigationController): Boolean