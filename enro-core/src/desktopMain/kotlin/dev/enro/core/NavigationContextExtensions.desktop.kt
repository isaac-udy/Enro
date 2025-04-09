package dev.enro.core

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.container.NavigationContainerManager

// Desktop implementation
internal actual val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = null // This should be implemented properly for desktop
actual val containerManager: NavigationContainerManager
    get() = TODO("Not yet implemented")