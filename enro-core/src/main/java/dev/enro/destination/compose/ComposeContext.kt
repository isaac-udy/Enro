package dev.enro.destination.compose

import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.OPEN_ARG
import dev.enro.core.activity
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.destination.activity
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.navigationController
import dev.enro.core.isActive
import dev.enro.core.requestClose
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        onBoundToNavigationHandle = {
            bindBackHandling(this, it)
        }
    )
}

private fun bindBackHandling(
    navigationContext: NavigationContext<out ComposableDestination>,
    navigationHandle: NavigationHandle
) {
    val backConfiguration = navigationContext.controller.config.backConfiguration

    when (backConfiguration) {
        is EnroBackConfiguration.Default -> {
            // Should be handled at the Activity level
        }

        is EnroBackConfiguration.Manual -> {
            // Do nothing
        }

        is EnroBackConfiguration.Predictive -> configurePredictiveBackHandling(navigationContext, navigationHandle)
    }
}

private fun configurePredictiveBackHandling(
    navigationContext: NavigationContext<out ComposableDestination>,
    navigationHandle: NavigationHandle
) {
    val activity = navigationContext.activity
    val callback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            navigationHandle.requestClose()
        }
    }
    activity.onBackPressedDispatcher.addCallback(navigationContext.lifecycleOwner, callback)
    navigationContext.isActive
        .onEach { isActive ->
            callback.isEnabled = isActive
        }
        .launchIn(navigationContext.lifecycleOwner.lifecycleScope)
}