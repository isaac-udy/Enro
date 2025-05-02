package dev.enro.animation

import dev.enro.core.AnyOpenInstruction

internal data class ClosingTransition(
    val priority: Int,
    val transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimation?
)