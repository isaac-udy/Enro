package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationHandle

internal expect fun ViewModel.getNavigationHandle(): NavigationHandle
