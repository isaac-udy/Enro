package dev.enro.core.controller.repository

import dev.enro.core.ClosingTransition
import dev.enro.core.NavigationAnimationOverride
import dev.enro.core.OpeningTransition

internal class NavigationAnimationRepository {
    private val opening = mutableListOf<OpeningTransition>()
    private val closing = mutableListOf<ClosingTransition>()

    val controllerOverrides: NavigationAnimationOverride = NavigationAnimationOverride(
        parent = null,
        opening = opening,
        closing = closing,
    )

    fun addAnimations(navigationAnimationOverride: NavigationAnimationOverride) {
        if (navigationAnimationOverride.parent != null) throw IllegalArgumentException("Can't add a NavigationAnimationOverride with a parent to the NavigationAnimationRepository")
        opening.addAll(navigationAnimationOverride.opening)
        closing.addAll(navigationAnimationOverride.closing)
    }
}