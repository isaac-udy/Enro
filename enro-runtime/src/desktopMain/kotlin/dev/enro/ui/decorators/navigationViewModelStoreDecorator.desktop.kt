package dev.enro.ui.decorators

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberShouldRemoveViewModelStoreCallback(): () -> Boolean {
    // On desktop, always remove ViewModelStore when destination is removed
    return { true }
}