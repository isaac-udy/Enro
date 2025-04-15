package dev.enro.core.controller.usecase

import dev.enro.animation.ClosingTransition
import dev.enro.animation.DefaultAnimations
import dev.enro.animation.NavigationAnimationOverride
import dev.enro.animation.NavigationAnimationTransition
import dev.enro.animation.OpeningTransition
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.controller.NavigationController

internal class GetNavigationAnimations(
    private val controller: NavigationController,
    internal val navigationAnimationOverride: NavigationAnimationOverride,
) {
    fun opening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition {
        if (earlyExitForNoAnimation()) return DefaultAnimations.noOp
        val override = overrideForOpening(exiting, entering)
        if (override != null) return override
        return DefaultAnimations.opening(exiting, entering)
    }

    fun closing(exiting: AnyOpenInstruction, entering: AnyOpenInstruction?): NavigationAnimationTransition {
        if (earlyExitForNoAnimation()) return DefaultAnimations.noOp
        val override = overrideForClosing(exiting, entering)
        if (override != null) return override
        return DefaultAnimations.closing(exiting, entering)
    }

    private fun overrideForOpening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition? {
        val opening = mutableMapOf<Int, MutableList<OpeningTransition>>()
        var override: NavigationAnimationOverride? = navigationAnimationOverride
        while(override != null) {
            override.opening.reversed().forEach {
                opening.getOrPut(it.priority) { mutableListOf() }
                    .add(it)
            }
            override = override.parent
        }
        opening.keys.sortedDescending()
            .flatMap { opening[it].orEmpty() }
            .forEach {
                return it.transition(exiting, entering) ?: return@forEach
            }
        return null
    }

    private fun overrideForClosing(exiting: AnyOpenInstruction, entering: AnyOpenInstruction?): NavigationAnimationTransition? {
        val closing = mutableMapOf<Int, MutableList<ClosingTransition>>()
        var override: NavigationAnimationOverride? = navigationAnimationOverride
        while(override != null) {
            override.closing.reversed().forEach {
                closing.getOrPut(it.priority) { mutableListOf() }
                    .add(it)
            }
            override = override.parent
        }
        closing.keys.sortedDescending()
            .flatMap { closing[it].orEmpty() }
            .forEach {
                return it.transition(exiting, entering) ?: return@forEach
            }
        return null
    }

    private fun earlyExitForNoAnimation() : Boolean {
        return isAnimationsDisabledForPlatform(controller) || controller.config.isAnimationsDisabled
    }
}

internal expect fun isAnimationsDisabledForPlatform(controller: NavigationController): Boolean