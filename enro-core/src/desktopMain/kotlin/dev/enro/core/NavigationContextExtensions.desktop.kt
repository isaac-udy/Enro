package dev.enro.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.internal.handle.getNavigationHandleViewModel

public actual fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    return containerManager.activeContainer?.childContext?.leafContext()
        ?: this
}

// Desktop implementation
internal actual val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = getNavigationHandleViewModel().navigationContext

public actual val containerManager: NavigationContainerManager
    @Composable
    get() {
        val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

        val lifecycleOwner = LocalLifecycleOwner.current

        // The navigation context attached to a NavigationHandle may change when the Context, View,
        // or LifecycleOwner changes, so we're going to re-query the navigation context whenever
        // any of these change, to ensure the container always has an up-to-date NavigationContext
        return remember(viewModelStoreOwner, lifecycleOwner) {
            viewModelStoreOwner
                .navigationContext!!
                .containerManager
        }
    }