package dev.enro.viewmodel

import androidx.lifecycle.ViewModelProvider
import dev.enro.NavigationHandle

public actual class EnroViewModelFactory actual constructor(
    navigationHandle: NavigationHandle<*>,
    delegate: ViewModelProvider.Factory,
) : ViewModelProvider.Factory {
    init {
        TODO()
    }
}