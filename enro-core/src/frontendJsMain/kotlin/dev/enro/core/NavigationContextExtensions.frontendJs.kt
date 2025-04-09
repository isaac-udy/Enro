package dev.enro.core

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.container.NavigationContainerManager

// Frontend JS implementation
internal actual val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = null // This should be implemented properly for frontendJs
actual val containerManager: NavigationContainerManager
    get() = TODO("Not yet implemented")