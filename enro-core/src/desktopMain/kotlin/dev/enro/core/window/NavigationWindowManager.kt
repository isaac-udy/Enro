package dev.enro.core.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ApplicationScope
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import dev.enro.destination.desktop.DesktopWindow
import dev.enro.destination.desktop.DesktopWindowNavigationBinding

public actual class NavigationWindowManager actual constructor(
    private val controller: NavigationController,
) : EnroPlugin() {

    private val desktopWindows = mutableStateOf(listOf<DesktopWindow>())

    public actual fun open(instruction: AnyOpenInstruction) {
        val binding = controller.bindingForKeyType(instruction.navigationKey::class) as DesktopWindowNavigationBinding
        val window = binding.constructDestination()
        desktopWindows.value = desktopWindows.value + window
    }
    public actual fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction?) {
        desktopWindows.value = desktopWindows.value.filter {
            it != context.contextReference
        }
        if (andOpen != null) {
            open(andOpen)
        }
    }

    @Composable
    public fun ApplicationScope.Render() {
        desktopWindows.value.forEach { window ->
            key(window) {
                with(window) {
                    ApplyLocals(controller) {
                        Render()
                    }
                }
            }
        }
    }
}