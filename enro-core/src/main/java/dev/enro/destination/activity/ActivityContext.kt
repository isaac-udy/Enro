package dev.enro.destination.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.navigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.destination.compose.ComposableDestination
import dev.enro.destination.compose.destination.activity

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

public val NavigationContext<*>.activity: ComponentActivity
    get() = when (contextReference) {
        is ComponentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.owner.activity
        else -> throw EnroException.UnreachableState()
    }

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : ComponentActivity> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

public val ComponentActivity.containerManager: NavigationContainerManager
    get() = navigationContext.containerManager