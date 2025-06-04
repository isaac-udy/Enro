package dev.enro.ui.decorators

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberShouldRemoveViewModelStoreCallback(): () -> Boolean {
    val activity = LocalActivity.current
    return { activity?.isChangingConfigurations != true }
}
