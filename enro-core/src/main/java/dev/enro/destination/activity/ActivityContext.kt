package dev.enro.destination.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.navigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel

internal fun <ContextType : ComponentActivity> ActivityContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.application.navigationController },
        getParentContext = { null },
        getArguments = { contextReference.intent.extras ?: Bundle() },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
    )
}

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : ComponentActivity> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

public val ComponentActivity.containerManager: NavigationContainerManager
    get() = navigationContext.containerManager