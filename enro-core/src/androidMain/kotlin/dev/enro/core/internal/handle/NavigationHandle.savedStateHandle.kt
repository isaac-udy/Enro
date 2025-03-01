package dev.enro.core.internal.handle

import androidx.lifecycle.SavedStateHandle
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.get

internal fun NavigationHandle.savedStateHandle(): SavedStateHandle {
    return dependencyScope.get()
}
