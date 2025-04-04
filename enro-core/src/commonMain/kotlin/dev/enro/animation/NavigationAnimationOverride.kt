package dev.enro.animation

internal data class NavigationAnimationOverride(
    val parent: NavigationAnimationOverride?,
    val opening: List<OpeningTransition>,
    val closing: List<ClosingTransition>,
)