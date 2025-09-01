package dev.enro.viewmodel

import androidx.lifecycle.ViewModelProvider
import dev.enro.NavigationHandle

public expect class EnroViewModelFactory(
    navigationHandle: NavigationHandle<*>,
    delegate: ViewModelProvider.Factory,
) : ViewModelProvider.Factory