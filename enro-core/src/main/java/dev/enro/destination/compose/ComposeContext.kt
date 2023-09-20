package dev.enro.destination.compose

import androidx.core.os.bundleOf
import dev.enro.core.NavigationContext
import dev.enro.core.OPEN_ARG
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.destination.activity
import dev.enro.core.controller.navigationController

internal fun <ContextType : ComposableDestination> ComposeContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.owner.activity.application.navigationController },
        getParentContext = { contextReference.owner.parentContainer.context },
        getArguments = { bundleOf(OPEN_ARG to contextReference.owner.instruction) },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
    )
}
