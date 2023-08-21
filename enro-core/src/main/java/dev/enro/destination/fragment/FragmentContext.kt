package dev.enro.destination.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.navigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.parentContainer
import dev.enro.destination.compose.parentContainer
import dev.enro.destination.activity.navigationContext

internal fun <ContextType : Fragment> FragmentContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.requireActivity().application.navigationController },
        getParentContext = {
            when (val parentFragment = contextReference.parentFragment) {
                null -> contextReference.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
        },
        getArguments = { contextReference.arguments ?: Bundle() },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
    )
}

public val NavigationContext<out Fragment>.fragment: Fragment
    get() = contextReference

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : Fragment> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

public val Fragment.containerManager: NavigationContainerManager
    get() = navigationContext.containerManager

public val Fragment.parentContainer: NavigationContainer?
    get() = navigationContext.parentContainer()