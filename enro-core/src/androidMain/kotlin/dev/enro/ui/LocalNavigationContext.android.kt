package dev.enro.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationContext
import dev.enro.platform.navigationContext

@Composable
internal actual fun findRootNavigationContext(): NavigationContext.Root {
    val activity = LocalActivity.current
    return remember(activity) {
        requireNotNull(activity)
        activity.navigationContext
    }
}