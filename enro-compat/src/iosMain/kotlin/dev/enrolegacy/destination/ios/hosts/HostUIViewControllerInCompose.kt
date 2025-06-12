package dev.enrolegacy.destination.ios.hosts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.destination.ios.EmbeddedEnroUIViewController
import dev.enro.destination.ios.UIViewControllerNavigationBinding
import kotlinx.serialization.Serializable

@Serializable
public data class HostUIViewControllerInCompose(
    val originalInstruction: NavigationInstruction.Open<*>,
) : NavigationKey.SupportsPush, NavigationKey.SupportsPresent

@Composable
internal fun HostUIViewControllerInComposeScreen() {
    val navigationHandle = navigationHandle<NavigationKey>()
    val instruction = remember(navigationHandle.instruction) {
        when(val key = navigationHandle.key) {
            is HostUIViewControllerInCompose -> key.originalInstruction
            else -> navigationHandle.instruction
        }
    }
    EmbeddedEnroUIViewController(
        instruction = instruction,
        factory = {
            val binding = requireNotNull(NavigationController.navigationController)
                .bindingForInstruction(instruction)
            requireNotNull(binding) {
                "UIViewControllerNavigationBinding expected, but got null"
            }
            require(binding is UIViewControllerNavigationBinding<*, *>) {
                "UIViewControllerNavigationBinding expected, but got ${binding::class}"
            }
            binding.constructDestination()
        },
    )
}