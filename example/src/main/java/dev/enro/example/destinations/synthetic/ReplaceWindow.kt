package dev.enro.example.destinations.synthetic

import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.rootContext
import dev.enro.destination.synthetic.syntheticDestination
import kotlinx.serialization.Serializable

@Serializable
class ReplaceWindow(
    val with: NavigationKey.SupportsPresent,
) : NavigationKey.SupportsPresent

@NavigationDestination(ReplaceWindow::class)
val replaceWindowDestination = syntheticDestination<ReplaceWindow> {
    val rootContext = navigationContext.rootContext()
    navigationContext.controller.windowManager.close(
        context = rootContext,
        andOpen = NavigationInstruction.Present(key.with)
    )
}
