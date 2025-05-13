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

    internal fun isExplicitWindowInstruction(instruction: AnyOpenInstruction): Boolean

    public companion object
}

/**
 * This is the key used to identify that the NavigationInstruction is intended to open a
 * new window, using the platform specific functionality to host the instruction in a window.
 *
 * Various platforms have different window types and window hosts:
 * Android: Activity (ActivityHostForAnyInstruction)
 * Desktop: Window (DesktopWindowHostForComposable)
 * iOS: Window (iOSWindowHostForComposable)
 * Web: Window (WebWindowHostForComposable)
 */
public val NavigationWindowManager.Companion.EXTRA_OPEN_IN_WINDOW: String
    get() = "dev.enro.core.window.OPEN_IN_WINDOW"
