package dev.enro.core.window

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin


public actual class NavigationWindowManager actual constructor(
    private val controller: NavigationController,
) : EnroPlugin() {

//    private val desktopWindows = mutableStateOf(listOf<DesktopWindow>())

    public actual fun open(instruction: AnyOpenInstruction) {

//        val newWindow = window.open(url.href, "_self")!!
//        newWindow.sessionStorage.set("sessionThing", "SeqrchQ")
    }

    public actual fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction?) {
        if (andOpen != null) {
            open(andOpen)
        }
    }
}