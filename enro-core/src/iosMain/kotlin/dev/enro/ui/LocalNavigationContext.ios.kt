package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import dev.enro.context.RootContext
import dev.enro.context.root
import dev.enro.platform.internalNavigationContext
import platform.UIKit.UIViewController

@Composable
internal actual fun findRootNavigationContext(): RootContext {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        requireNotNull(viewController)
        var active: UIViewController? = viewController
        while (active != null) {
            val context = active.internalNavigationContext
            if (context != null) {
                return@remember context.root()
            }
            active = active.parentViewController
        }
        error("Could not find a RootContext in the parent view controller hierarchy from $viewController")
    }
}