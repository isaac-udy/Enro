package dev.enro.core.controller.repository

import dev.enro.animation.ClosingTransition
import dev.enro.animation.NavigationAnimation
import dev.enro.animation.NavigationAnimationOverride
import dev.enro.animation.OpeningTransition
import kotlin.reflect.KClass

internal class NavigationAnimationRepository {
    val defaults = mutableMapOf<KClass<out NavigationAnimation>, NavigationAnimation.Defaults<out NavigationAnimation>>()
    private val opening = mutableListOf<OpeningTransition>()
    private val closing = mutableListOf<ClosingTransition>()

    val controllerOverrides: NavigationAnimationOverride = NavigationAnimationOverride(
        parent = null,
        opening = opening,
        closing = closing,
        defaults = defaults,
    )

    fun addAnimations(navigationAnimationOverride: NavigationAnimationOverride) {
        if (navigationAnimationOverride.parent != null) throw IllegalArgumentException("Can't add a NavigationAnimationOverride with a parent to the NavigationAnimationRepository")
        opening.addAll(navigationAnimationOverride.opening)
        closing.addAll(navigationAnimationOverride.closing)
        navigationAnimationOverride.defaults.forEach { (key, value) ->
            defaults[key] = value
        }
    }
}