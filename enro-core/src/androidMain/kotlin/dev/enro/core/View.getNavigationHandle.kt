package dev.enro.core

import android.view.View
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import dev.enro.core.internal.handle.getNavigationHandleViewModel

public fun View.getNavigationHandle(): NavigationHandle? =
    findViewTreeViewModelStoreOwner()?.getNavigationHandleViewModel()

public fun View.requireNavigationHandle(): NavigationHandle {
    if (!isAttachedToWindow) {
        throw EnroException.InvalidViewForNavigationHandle("$this is not attached to any Window, which is required to retrieve a NavigationHandle")
    }
    val viewModelStoreOwner = findViewTreeViewModelStoreOwner()
        ?: throw EnroException.InvalidViewForNavigationHandle("Could not find ViewTreeViewModelStoreOwner for $this, which is required to retrieve a NavigationHandle")
    return viewModelStoreOwner.getNavigationHandleViewModel()
}