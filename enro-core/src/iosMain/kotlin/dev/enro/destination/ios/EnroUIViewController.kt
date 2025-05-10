package dev.enro.destination.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.core.container.backstackOf
import platform.UIKit.UIViewController

/**
 * Creates a UIViewController for a NavigationInstruction.Open where the NavigationKey for
 * that instruction is bound to a Composable function. The resulting UIViewController will
 * host the content of the Composable function.
 */
public fun EnroUIViewController(
    instruction: AnyOpenInstruction,
): UIViewController {
    val controller = EnroComposeUIViewController {
        val rootContainer = rememberNavigationContainer(
            initialBackstack = backstackOf(instruction),
            emptyBehavior = EmptyBehavior.Action {
                true
            },
            filter = accept {
                instruction { it == instruction }
            }
        )
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            rootContainer.Render()
        }
    }
    controller.navigationInstruction = instruction
    return controller
}

/**
 * Wraps a UIViewController provided in the controller lambda in an EnroComposeUIViewController,
 * allowing that UIViewController to be used within Enro.
 */
public fun EnroUIViewController(
    instruction: AnyOpenInstruction,
    controller: () -> UIViewController,
): UIViewController {
    val composeController = EnroComposeUIViewController {
        EmbeddedEnroUIViewController(
            instruction = instruction,
            factory = controller,
        )
    }
    composeController.navigationInstruction = instruction
    return composeController
}
