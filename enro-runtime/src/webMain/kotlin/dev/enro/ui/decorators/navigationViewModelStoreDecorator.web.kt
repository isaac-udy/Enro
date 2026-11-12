package dev.enro.ui.decorators

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberShouldRemoveViewModelStoreCallback(): () -> Boolean {
    // Web removes the ViewModelStore each time a destination is removed.
    return { true }
}