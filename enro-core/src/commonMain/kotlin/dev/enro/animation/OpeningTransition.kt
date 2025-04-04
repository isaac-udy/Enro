package dev.enro.animation

import dev.enro.core.AnyOpenInstruction

internal data class OpeningTransition(
    val priority: Int,
    val transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimationTransition?
)