package dev.enro.core.window

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin

public actual class NavigationWindowManager actual constructor(
    controller: NavigationController,
) : EnroPlugin() {
    public actual fun open(instruction: AnyOpenInstruction) {}
    public actual fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction?) {}
}
