package dev.enro.destination.ios

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.window.navigationContext
import dev.enro.destination.compose.navigationContext
import platform.UIKit.UIViewController

@Composable
internal fun EmbedEnroUIKitViewController(
    instruction: AnyOpenInstruction,
    factory: () -> UIViewController,
) {
    key(instruction) {
        val context = navigationContext
        UIKitViewController(
            modifier = Modifier.fillMaxSize(),
            factory = {
                factory().apply {
                    this.navigationContext = context
                    this.navigationInstruction = instruction
                }
            },
        )
    }
}