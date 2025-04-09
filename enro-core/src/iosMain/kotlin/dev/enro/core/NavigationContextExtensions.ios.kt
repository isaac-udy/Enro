package dev.enro.core

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.container.NavigationContainerManager

// iOS implementation
internal actual val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = null // This should be implemented properly for iOS

public actual val containerManager: NavigationContainerManager
    get() = TODO("Not yet implemented")