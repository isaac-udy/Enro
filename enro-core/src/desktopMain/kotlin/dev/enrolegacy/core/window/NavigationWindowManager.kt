package dev.enrolegacy.core.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.asPresent
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import dev.enro.destination.compose.ComposableNavigationBinding
import dev.enro.destination.desktop.DesktopWindow
import dev.enro.destination.desktop.DesktopWindowNavigationBinding
import dev.enro.destination.desktop.OpenComposableInDesktopWindow

public actual class NavigationWindowManager actual constructor(
    private val controller: NavigationController,
) : EnroPlugin() {

    public val desktopWindows: MutableState<List<DesktopWindow>> = mutableStateOf(listOf<DesktopWindow>())

    public actual fun open(instruction: AnyOpenInstruction) {
        val binding = controller.bindingForInstruction(instruction)
        val window = when (binding) {
            is DesktopWindowNavigationBinding -> binding.constructDestination()
            is ComposableNavigationBinding -> {
                open(
                    OpenComposableInDesktopWindow(instruction).asPresent()
                )
                return
            }
            else -> error(
                "Attempted to open window for a NavigationInstruction with key of type ${instruction.navigationKey::class}, which not bound to a DesktopWindow"
            )
        } as DesktopWindow

        window.instruction = instruction
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

    internal actual fun isExplicitWindowInstruction(instruction: AnyOpenInstruction): Boolean {
        return instruction.isOpenInWindow() || controller.bindingForInstruction(instruction) is DesktopWindowNavigationBinding<*, *>
    }

    @Composable
    public fun Render() {
        desktopWindows.value.forEach { window ->
            key(window) {
                window.ApplyLocals(controller) {
                    window.Render()
                }
            }
        }
    }
    public actual companion object
}