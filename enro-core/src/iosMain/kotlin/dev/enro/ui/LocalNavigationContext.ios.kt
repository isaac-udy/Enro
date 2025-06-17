package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import dev.enro.context.RootContext
import dev.enro.platform.navigationContext

@Composable
internal actual fun findRootNavigationContext(): RootContext {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        requireNotNull(viewController)
        viewController.navigationContext
    }
}