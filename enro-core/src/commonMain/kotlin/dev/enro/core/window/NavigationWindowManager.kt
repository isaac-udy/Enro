package dev.enro.core.window

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin

public expect class NavigationWindowManager(
    controller: NavigationController,
) : EnroPlugin {
    public fun open(instruction: AnyOpenInstruction)
    public fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction? = null)
}
