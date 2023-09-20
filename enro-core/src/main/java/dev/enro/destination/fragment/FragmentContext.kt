package dev.enro.destination.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationContext
import dev.enro.core.controller.navigationController
import dev.enro.core.navigationContext

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
