package dev.enro3.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable

@Composable
internal actual fun shouldRemoveViewModelStoreCallback(): () -> Boolean {
    val activity = LocalActivity.current
    return { activity?.isChangingConfigurations != true }
}